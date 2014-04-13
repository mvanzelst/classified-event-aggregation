package org.classified_event_aggregation.storm_input_topology;

import java.io.Serializable;
import java.util.Arrays;

import org.classified_event_aggregation.storm_input_topology.function.DurationAnomalyDetection;
import org.classified_event_aggregation.storm_input_topology.function.ExceptionCountAnomalyDetection;
import org.classified_event_aggregation.storm_input_topology.model.LogMessage;
import org.classified_event_aggregation.storm_input_topology.persistence.LogMessageStoreStateFactory;
import org.classified_event_aggregation.storm_input_topology.persistence.LogMessageStoreUpdater;
import org.classified_event_aggregation.storm_input_topology.persistence.NotificationStoreStateFactory;
import org.classified_event_aggregation.storm_input_topology.persistence.NotificationStoreUpdater;
import org.classified_event_aggregation.storm_input_topology.storm.ParseJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.kafka.KafkaConfig.StaticHosts;
import storm.kafka.trident.TransactionalTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFilter;
import storm.trident.spout.IBatchSpout;
import storm.trident.tuple.TridentTuple;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.RawMultiScheme;
import backtype.storm.tuple.Fields;

public class LogMessagesAnomalyDetectionTopologyBuilder implements Serializable {
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(LogMessagesAnomalyDetectionTopologyBuilder.class);

	private IBatchSpout logRecordSpout;
	private LogMessageStoreStateFactory logMessageStoreStateFactory;
	private NotificationStoreStateFactory notificationStoreStateFactory;

	public StormTopology buildTopology() {

		TridentTopology topology = new TridentTopology();
		Stream logRecordStream = getInputStream(topology);

		Stream logSequenceStream = logRecordStream
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
			.each(new Fields("log_sequence"), new ExceptionCountAnomalyDetection(), new Fields("description", "relevance", "timestamp", "algorithm_name"))
			// Store the notification
			.partitionPersist(getNotificationStoreStateFactory(), new Fields("description", "relevance", "timestamp", "algorithm_name", "log_sequence"), new NotificationStoreUpdater());

		// Check for anomalies in the amount of log records

		// Check if sequence duration thresholds have been exceeded

		// Check if sequence duration is anomalous (past the six sigma)
		logSequenceStream
			.each(new Fields("log_sequence"), new DurationAnomalyDetection(), new Fields("description", "relevance", "timestamp", "algorithm_name"))
			// Store the notification
			.partitionPersist(getNotificationStoreStateFactory(), new Fields("description", "relevance", "timestamp", "algorithm_name", "log_sequence"), new NotificationStoreUpdater());

		return topology.build();
	}
	
	/**
	 * This method is used by the storm cluster to create the topology
	 * It can also be run stand-alone as a local cluster
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// @todo Add database conf
		Config topologyConf = new Config();
		topologyConf.setMaxSpoutPending(5);
		LogMessagesAnomalyDetectionTopologyBuilder self = new LogMessagesAnomalyDetectionTopologyBuilder();
		if (args.length == 0) {
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("classifiedEventProcessor", topologyConf, self.buildTopology());
		} else {
			topologyConf.setNumWorkers(3);
			StormSubmitter.submitTopology(args[0], topologyConf, self.buildTopology());
		}
	}

	/* Allow integration tests to inject data source and data storage components */
	
	/* Input Stream */
	
	private Stream getInputStream(TridentTopology topology){
		if(logRecordSpout == null){
			TridentKafkaConfig spoutConfig = new TridentKafkaConfig(
					StaticHosts.fromHostString(
							Arrays.asList(new String[] { "localhost" }), 1), "logs");
			spoutConfig.scheme = new RawMultiScheme();
			TransactionalTridentKafkaSpout spout = new TransactionalTridentKafkaSpout(spoutConfig);
			return topology.newStream("logSequenceStream", spout);
		} else {
			return topology.newStream("logSequenceStream", logRecordSpout);
		}
	}

	public IBatchSpout getLogRecordSpout() {
		return logRecordSpout;
	}

	public void setLogRecordSpout(IBatchSpout logRecordSpout) {
		this.logRecordSpout = logRecordSpout;
	}

	/* Notification Storage */
	
	private NotificationStoreStateFactory getNotificationStorage(){
		if(notificationStoreStateFactory == null){
			return new NotificationStoreStateFactory();
		} else {
			return notificationStoreStateFactory;
		}
	}

	public NotificationStoreStateFactory getNotificationStoreStateFactory() {
		return notificationStoreStateFactory;
	}

	public void setNotificationStoreStateFactory(NotificationStoreStateFactory notificationStoreStateFactory) {
		this.notificationStoreStateFactory = notificationStoreStateFactory;
	}
	
	/* LogMessage Storage */
	
	private LogMessageStoreStateFactory getLogMessageStorages(){
		if(logMessageStoreStateFactory == null){
			return new LogMessageStoreStateFactory();
		} else {
			return logMessageStoreStateFactory;
		}
	}

	public LogMessageStoreStateFactory getLogMessageStoreStateFactory() {
		return logMessageStoreStateFactory;
	}

	public void setLogMessageStoreStateFactory(LogMessageStoreStateFactory logMessageStoreStateFactory) {
		this.logMessageStoreStateFactory = logMessageStoreStateFactory;
	}
	
}