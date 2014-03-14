package org.classified_event_aggregation.storm_input_topology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.classified_event_aggregation.storm_input_topology.model.LogMessage;
import org.classified_event_aggregation.storm_input_topology.model.LogSequence;
import org.classified_event_aggregation.storm_input_topology.persistence.LogMessageStoreStateFactory;
import org.classified_event_aggregation.storm_input_topology.persistence.LogMessageStoreUpdater;
import org.classified_event_aggregation.storm_input_topology.persistence.NotificationStoreStateFactory;
import org.classified_event_aggregation.storm_input_topology.persistence.NotificationStoreUpdater;
import org.classified_event_aggregation.storm_input_topology.storm.ParseJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

import storm.kafka.KafkaConfig.StaticHosts;
import storm.kafka.trident.TransactionalTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFilter;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.RawMultiScheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class LogMessagesAnomalyDetectionTopology {
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(LogMessagesAnomalyDetectionTopology.class);

	@SuppressWarnings("serial")
	public static StormTopology buildTopology(Map<String, Object> conf) {
		TridentKafkaConfig spoutConfig = new TridentKafkaConfig(
				StaticHosts.fromHostString(
						Arrays.asList(new String[] { "localhost" }), 1), "test");
		spoutConfig.scheme = new RawMultiScheme();
		TridentTopology topology = new TridentTopology();

		TransactionalTridentKafkaSpout spout = new TransactionalTridentKafkaSpout(spoutConfig);

		Stream logSequenceStream = topology
			.newStream("logSequenceStream", spout)
			.parallelismHint(1)
			.each(new Fields("bytes"), new ParseJSON(), new Fields("log_message"))
			.each(new Fields("log_message"), new BaseFilter() {

				@Override
				public boolean isKeep(TridentTuple tuple) {
					LogMessage logMessage = (LogMessage) tuple.getValueByField("log_message");
					return
						logMessage.getClassifications().containsKey("SEQUENCE_ID") &&
						logMessage.getClassifications().containsKey("SEQUENCE_NAME");
				}
			})
			// Store the log messages and reemit all messages (bundled) if the end of a sequence was received
			.partitionPersist(new LogMessageStoreStateFactory(), new Fields("log_message"), new LogMessageStoreUpdater(), new Fields("log_sequence"))
			.newValuesStream();

		/*
		 * Log Sequence Processing 
		 */

		// Check for anomalies in the amount of exceptions
		logSequenceStream
			.each(new Fields("log_sequence"), new BaseFunction() {

				// @TODO this could become very large
				// List for each unique sequenceName with a history of the amount of exceptions
				private Map<String, List<Integer>> sequenceExceptionsMap = new HashMap<>();

				@Override
				public void execute(TridentTuple tuple, TridentCollector collector) {
					LogSequence logSequence = (LogSequence) tuple.getValueByField("log_sequence");
					List<Integer> sequenceExceptions;
					if(sequenceExceptionsMap.containsKey(logSequence.getSequenceName())){
						sequenceExceptions = sequenceExceptionsMap.get(logSequence.getSequenceName());
					} else {
						sequenceExceptions = new ArrayList<>();
					}

					// Count the number of exceptions in the current log sequence
					int numExceptions = countExceptions(logSequence);

					// What is the maximum number of exceptions
					int maxExceptions = Collections.max(sequenceExceptions);

					/*
					 * Output notification
					 */
					String algorithmName = "Amount of exceptions anomaly";
					String description;

					// The timestamp of the last logmessage
					long timestamp = Iterables.getLast(logSequence.getLogMessages()).getTimestamp();

					double relevance;

					if(numExceptions > maxExceptions){
						description = "An anomalous exception occured";

						// @TODO the relevance could be based on couple of things:
						// The number of previous values
						// The standard deviation of the set
						// The change with respect to the stddev
						relevance = 1;
					} else {
						description = "No anomalous exceptions occured";
						relevance = 0;
					}

					collector.emit(new Values(description, relevance, timestamp, algorithmName));

					// Store the numExceptions of the current LogSequence
					sequenceExceptions.add(numExceptions);

					// Cap the list
					if(sequenceExceptions.size() > 100)
						sequenceExceptions.remove(0);

					// Store the list
					sequenceExceptionsMap.put(logSequence.getSequenceName(), sequenceExceptions);
				}

				private int countExceptions(LogSequence logSequence){
					int numExceptions = 0;
					for (LogMessage logMessage : logSequence.getLogMessages()) {
						if(logMessage.getClassifications().containsKey("LOG_LEVEL") && logMessage.getClassifications().get("LOG_LEVEL").getValue().contentEquals("ERROR"))
							numExceptions++;
					}
					return numExceptions;
				}

			}, new Fields("description", "relevance", "timestamp", "algorithm_name"))
			// Store the notification
			.partitionPersist(new NotificationStoreStateFactory(), new Fields("description", "relevance", "timestamp", "algorithm_name", "log_sequence"), new NotificationStoreUpdater());

		// Check for anomalies in the amount of log records

		// Check if sequence duration thresholds have been exceeded

		// Check if sequence duration is anomalous (six sigma) 

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