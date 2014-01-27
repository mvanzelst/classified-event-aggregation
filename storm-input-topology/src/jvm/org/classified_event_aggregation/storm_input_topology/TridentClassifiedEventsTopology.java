package org.classified_event_aggregation.storm_input_topology;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.classified_event_aggregation.storm_input_topology.persistence.CassandraEventStore;
import org.classified_event_aggregation.storm_input_topology.persistence.EventStoreStateFactory;
import org.classified_event_aggregation.storm_input_topology.persistence.EventStoreUpdater;
import org.classified_event_aggregation.storm_input_topology.storm.ParseJSON;
import org.classified_event_aggregation.storm_input_topology.storm.TestSpoutFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.kafka.HostPort;
import storm.kafka.KafkaConfig.BrokerHosts;
import storm.kafka.KafkaConfig.StaticHosts;
import storm.kafka.SpoutConfig;
import storm.trident.TridentState;
import storm.trident.TridentTopology;
import storm.trident.testing.FixedBatchSpout;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.RawScheme;
import backtype.storm.tuple.Fields;

public class TridentClassifiedEventsTopology {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public static StormTopology buildTopology(Map<String, Object> conf) {
		BrokerHosts brokerHosts = new StaticHosts(Arrays.asList(new HostPort("localhost", 9092)), 1);
		SpoutConfig spoutConfig = new SpoutConfig(
			brokerHosts, // list of Kafka
			"test", // topic
			"/kafkastorm", // the root path in Zookeeper for the spout to store the consumer offsets
			"discovery" // an id for this consumer for storing the consumer offsets in Zookeeper
		); 
		spoutConfig.scheme = new RawScheme();
		//KafkaSpout spout = new KafkaSpout(spoutConfig);
		FixedBatchSpout spout = TestSpoutFactory.build();
		TridentTopology topology = new TridentTopology();
		topology
				.newStream("spout1", spout)
				.parallelismHint(1)
				.each(new Fields("bytes"), new ParseJSON(), new Fields("description", "timestamp", "classifications"))
				.partitionPersist(new EventStoreStateFactory() , new Fields("description", "timestamp", "classifications"), new EventStoreUpdater());
		
		return topology.build();
	}

	public static void main(String[] args) throws Exception {
		// @todo Add database conf
		Config topologyConf = new Config();
		topologyConf.put("databaseType", "cassandra");
		topologyConf.setMaxSpoutPending(20);
		
		if (args.length == 0) {
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("classifiedEventProcessor", topologyConf, buildTopology(topologyConf));
		} else {
			topologyConf.setNumWorkers(3);
			StormSubmitter.submitTopology(args[0], topologyConf, buildTopology(topologyConf));
		}
	}
}