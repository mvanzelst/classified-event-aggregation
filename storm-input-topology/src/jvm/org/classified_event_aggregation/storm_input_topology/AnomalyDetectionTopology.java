package org.classified_event_aggregation.storm_input_topology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.classified_event_aggregation.storm_input_topology.model.Classification;
import org.classified_event_aggregation.storm_input_topology.persistence.EventStoreStateFactory;
import org.classified_event_aggregation.storm_input_topology.persistence.EventStoreUpdater;
import org.classified_event_aggregation.storm_input_topology.storm.ParseJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.kafka.KafkaConfig.StaticHosts;
import storm.kafka.trident.TransactionalTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
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
		topology
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
						return hasTaskId && hasTaskName;
					}
				})
				// @TODO shuffle and split by task_name
				.each(new Fields("classifications", "timestamp"), new TaskDurationAnomolyDetection(), new Fields())
				.parallelismHint(1);

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

	@SuppressWarnings("serial")
	public static class TaskDurationAnomolyDetection implements Function {
		@SuppressWarnings("unused")
		private final Logger log = LoggerFactory.getLogger(AnomalyDetectionTopology.class);

		private Cache cache = null;

		private int taskMaxAge;
		private ScheduledExecutorService executor;

		@Override
		public void prepare(Map conf, TridentOperationContext context) {
			// @TODO make this configurable
			CacheConfiguration cacheConfig = new CacheConfiguration();
			cacheConfig.setMaxBytesLocalHeap(1024L * 1024L * 64L); // 64 MB
			cacheConfig.setName(UUID.randomUUID().toString());
			cache = new Cache(cacheConfig);
			CacheManager.create().addCache(cache);

			taskMaxAge = 60000;
			executor = Executors.newScheduledThreadPool(1);
			final TaskDurationAnomolyDetection ref = this;
			executor.scheduleAtFixedRate(new Runnable() {

				@SuppressWarnings("unused")
				private final Logger log = LoggerFactory.getLogger(AnomalyDetectionTopology.class);

				@Override
				public void run() {
					log.trace("Tick");
					try {
						ref.tick();
					} catch (Exception e){
						log.error("Failed to execute tick", e);
					}
					log.trace("Done tick");
				}
			}, 0L, 2000L, TimeUnit.MILLISECONDS);
		}

		@Override
		public void cleanup() {
			cache = null;
			try {
				executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				throw new RuntimeException("Failed to stop TaskDurationAnomolyDetection", e);
			}
		}

		@Override
		public void execute(TridentTuple tuple, TridentCollector collector) {
			Collection<Classification> classifications = (Collection<Classification>) tuple.getValueByField("classifications");
			String taskName = null, taskId = null, taskStatus = null;
			for (Classification classification : classifications) {
				if(classification.getKey().contentEquals("TASK_ID")){
					taskId = classification.getValue();
				} else if(classification.getKey().contentEquals("TASK_NAME")){
					taskName = classification.getValue();
				} else if(classification.getKey().contentEquals("TASK_STATUS")){
					taskStatus = classification.getValue();
				}
			}
			// The logrecord doesn't contain a starting or done tag for the TASK_STATUS
			if(taskStatus == null){
				return;
			}

			if(taskStatus.contentEquals("STARTING")){
				startTaskEvent(taskName, taskId, tuple.getLongByField("timestamp"));
			} else if(taskStatus.contentEquals("DONE")){
				finishedTaskEvent(taskName, taskId, tuple.getLongByField("timestamp"));
			} else {
				log.warn("Unrecognized taskStatus '{}'", taskStatus);
			}
		}

		private Map<String, Map<String, Object>> startTaskEvents = new ConcurrentHashMap<>();
		private Map<String, Map<String, Object>> finishedTasksEvents = new ConcurrentHashMap<>();
		private Map<String, DescriptiveStatistics> durationsPerTask = new ConcurrentHashMap<>();

		private void startTaskEvent(String taskName, String taskId, long timestamp){
			Map<String, Object> taskInfo = new HashMap<>();
			taskInfo.put("taskName", taskName);
			taskInfo.put("timestamp", timestamp);
			startTaskEvents.put(taskId, taskInfo);

			log.debug("Found start of task event, taskName '{}', taskId {} ", taskName, taskId);
		}

		private void finishedTaskEvent(String taskName, String taskId, long timestamp){
			Map<String, Object> taskInfo = new HashMap<>();
			taskInfo.put("taskName", taskName);
			taskInfo.put("timestamp", timestamp);
			finishedTasksEvents.put(taskId, taskInfo);

			log.debug("Found end of task event, taskName '{}', taskId {} ", taskName, taskId);
		}

		public synchronized void tick(){
			if(log.isDebugEnabled()){
				log.debug("startTaskEvents has items {}", startTaskEvents.entrySet().iterator().hasNext());
				log.debug("finishedTasksEvents has items {}", finishedTasksEvents.entrySet().iterator().hasNext());
			}
			// Check for each started task if there is a matching finished task
			for (Iterator<Entry<String, Map<String, Object>>> iter = startTaskEvents.entrySet().iterator(); iter.hasNext();) {
				Entry<String, Map<String, Object>> entry = iter.next();
				if(finishedTasksEvents.containsKey(entry.getKey())){
					// We've found a finished task
					Map<String, Object> startedTaskInfo = entry.getValue();
					String taskName = (String) startedTaskInfo.get("taskName");
					String taskId = entry.getKey();
					Map<String, Object> finishedTaskInfo = finishedTasksEvents.get(taskId);

					// Calculate the duration of the task
					long duration = (long) finishedTaskInfo.get("timestamp") - (long) startedTaskInfo.get("timestamp");

					// Print duration
					log.info("Task '{}' with id '{}' was completed after {} ms", taskName, taskId, duration);

					// Check if there is already duration data for this task
					DescriptiveStatistics taskDurations = durationsPerTask.get(taskName);
					if(taskDurations == null){
						// Only keep 100 datapoints
						taskDurations = new DescriptiveStatistics(100);
					} else {
						double max = taskDurations.getMax();

						// Check if the new value is the new max
						if(duration > max){
							log.warn("Task '{}' with id '{}' has new maximum of {} ms the previous one was {} ms", taskName, taskId, duration, (long) max);
						}
					}
					// Store the duration per task on the list
					taskDurations.addValue(duration);

					durationsPerTask.put(taskName, taskDurations);

					// Delete the processed task from both queues
					finishedTasksEvents.remove(taskId);
					iter.remove();
				}
			}

			// Remove start events older than ${task_max_age} ms
			for (Iterator<Entry<String, Map<String, Object>>> iter = startTaskEvents.entrySet().iterator(); iter.hasNext();) {
				Entry<String, Map<String, Object>> entry = iter.next();
				long age = System.currentTimeMillis() - (long) entry.getValue().get("timestamp");
				if(age > taskMaxAge){
					iter.remove();
					// @TODO store this
					log.warn("Task '{}' with id '{}' expired after {} ms", entry.getValue().get("taskName"), entry.getValue().get("taskId"), age);
				}
			}

			// Remove finished events older than ${task_max_age} ms
			for (Iterator<Entry<String, Map<String, Object>>> iter = finishedTasksEvents.entrySet().iterator(); iter.hasNext();) {
				Entry<String, Map<String, Object>> entry = iter.next();
				long age = System.currentTimeMillis() - (long) entry.getValue().get("timestamp");
				if(age > taskMaxAge){
					iter.remove();
					// @TODO store this
					log.error("Task '{}' with id '{}' was not started after receiving task completion message {} ms ago, this should not happen!", entry.getValue().get("taskName"), entry.getValue().get("taskId"), age);
				}
			}
		}
	}
}