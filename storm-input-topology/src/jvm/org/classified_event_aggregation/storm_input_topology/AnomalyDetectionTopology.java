package org.classified_event_aggregation.storm_input_topology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.classified_event_aggregation.storm_input_topology.function.TaskDurationDetection;
import org.classified_event_aggregation.storm_input_topology.model.Classification;
import org.classified_event_aggregation.storm_input_topology.persistence.EventStoreStateFactory;
import org.classified_event_aggregation.storm_input_topology.persistence.EventStoreUpdater;
import org.classified_event_aggregation.storm_input_topology.persistence.TaskDurationStoreStateFactory;
import org.classified_event_aggregation.storm_input_topology.persistence.TaskDurationUpdater;
import org.classified_event_aggregation.storm_input_topology.storm.ParseJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.kafka.KafkaConfig.StaticHosts;
import storm.kafka.trident.TransactionalTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
import storm.trident.Stream;
import storm.trident.TridentState;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFilter;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.Filter;
import storm.trident.operation.Function;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.TridentOperationContext;
import storm.trident.tuple.TridentTuple;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.RawMultiScheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class AnomalyDetectionTopology {
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(AnomalyDetectionTopology.class);

	@SuppressWarnings("serial")
	public static StormTopology buildTopology(Map<String, Object> conf) {
		TridentKafkaConfig spoutConfig = new TridentKafkaConfig(
				StaticHosts.fromHostString(
						Arrays.asList(new String[] { "localhost" }), 1), "test");
		spoutConfig.scheme = new RawMultiScheme();
		TridentTopology topology = new TridentTopology();
		TransactionalTridentKafkaSpout spout = new TransactionalTridentKafkaSpout(spoutConfig);
		Stream taskDurationStream = topology
				.newStream("spout2", spout)
				.parallelismHint(1)
				.each(new Fields("bytes"), new ParseJSON(), new Fields("description", "timestamp"))
				.each(new Fields("description"), new BaseFunction() {

					@SuppressWarnings("unused")
					private final Logger log = LoggerFactory.getLogger(AnomalyDetectionTopology.class);

					private Pattern p = Pattern.compile("#[A-Za-z+0-9_-]+:[A-Za-z+0-9_-]+");

					@Override
					public void execute(TridentTuple tuple, TridentCollector collector) {
						String description = tuple.getStringByField("description");
						Matcher m = p.matcher(description);
						Collection<Classification> classifications = new ArrayList<>();
						while(m.find()){
							log.debug("Found group '{}'", m.group());
							Classification c;
							try {
								c = Classification.fromString(m.group());
							} catch(RuntimeException e){
								log.error("Failed to parse classification", e);
								continue;
							}
							classifications.add(c);
						}
						log.debug("Parsed classifications '{}'", classifications);
						collector.emit(new Values(classifications));
					}
				}, new Fields("classifications"))
				// Filter events without the task_id and task_name classification
				.each(new Fields("classifications"), new BaseFilter() {

					@Override
					public boolean isKeep(TridentTuple tuple) {
						Collection<Classification> classifications = (Collection<Classification>) tuple.getValueByField("classifications");
						boolean hasTaskId = false;
						boolean hasTaskName = false;
						boolean hasTaskStatus = false;
						for (Classification classification : classifications) {
							if(classification.getKey().contentEquals("TASK_ID")){
								hasTaskId = true;
							} else if(classification.getKey().contentEquals("TASK_NAME")){
								hasTaskName = true;
							} else if(classification.getKey().contentEquals("TASK_STATUS")){
								hasTaskStatus = true;
							}
						}
						return hasTaskId && hasTaskName && hasTaskStatus;
					}
				})
				// @TODO shuffle and split by task_name
				.each(new Fields("classifications", "timestamp"), new TaskDurationDetection(), new Fields("task_name", "duration", "start_timestamp", "end_timestamp", "task_id"))
				.parallelismHint(1);

		taskDurationStream.partitionPersist(new TaskDurationStoreStateFactory(), new Fields("task_name", "duration", "start_timestamp", "end_timestamp", "task_id"), new TaskDurationUpdater());

		// @TODO build anomaly detection here 
		taskDurationStream.each(new Fields("task_name", "duration", "start_timestamp", "end_timestamp", "task_id"), new BaseFunction() {

			@SuppressWarnings("unused")
			private final Logger log = LoggerFactory.getLogger(AnomalyDetectionTopology.class);

			// @TODO this could become very large
			private Map<String, Map<String, Object>> taskDurations = new HashMap<>();

			@Override
			public void execute(TridentTuple tuple, TridentCollector collector) {
				Map<String, Object> taskInfo;
				if(taskDurations.containsKey(tuple.getStringByField("task_name"))){
					taskInfo = taskDurations.get(tuple.getStringByField("task_name"));
				} else {
					taskInfo = new HashMap<>();
					taskInfo.put("task_name", tuple.getStringByField("task_name"));
					DescriptiveStatistics stats = new DescriptiveStatistics();
					taskInfo.put("durations", stats);
					taskInfo.put("start_timestamp", tuple.getLongByField("start_timestamp"));
					taskInfo.put("end_timestamp", tuple.getLongByField("end_timestamp"));
					taskInfo.put("task_id", tuple.getStringByField("task_id"));
				}

				DescriptiveStatistics stats = (DescriptiveStatistics) taskInfo.get("durations");
				if(aboveSixSigma(stats, tuple.getLongByField("duration"))){
					collector.emit(tuple.getValues());
				}
				stats.addValue(tuple.getLongByField("duration"));
				taskDurations.put(tuple.getStringByField("task_name"), taskInfo);
			}

			private boolean aboveSixSigma(DescriptiveStatistics stats, double number){
				double aboveMean = number - stats.getMean();
				double aboveMeanInStdDev = aboveMean / stats.getStandardDeviation();
				if(aboveMeanInStdDev >= 6){
					log.info("{} is above sixth sigma with sigma of {} ", number, aboveMeanInStdDev);
					return true;
				} else {
					log.debug("{} is below sixth sigma with sigma of {} ", number, aboveMeanInStdDev);
					return false;
				}
			}

		}, new Fields(""));
		return topology.build();
	}

	public static void main(String[] args) throws Exception {
		// @todo Add database conf
		Config topologyConf = new Config();
		topologyConf.put("databaseType", "cassandra");
		topologyConf.setMaxSpoutPending(5);
		if (args.length == 0) {
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("classifiedEventProcessor", topologyConf, buildTopology(topologyConf));
		} else {
			topologyConf.setNumWorkers(3);
			StormSubmitter.submitTopology(args[0], topologyConf, buildTopology(topologyConf));
		}
	}

	
}