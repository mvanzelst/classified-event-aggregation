package org.classified_event_aggregation.storm_input_topology;

import java.util.Arrays;
import java.util.Map;

import org.classified_event_aggregation.storm_input_topology.persistence.EventStoreStateFactory;
import org.classified_event_aggregation.storm_input_topology.persistence.EventStoreUpdater;
import org.classified_event_aggregation.storm_input_topology.storm.ParseJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.kafka.HostPort;
import storm.kafka.KafkaConfig.BrokerHosts;
import storm.kafka.KafkaConfig.StaticHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.trident.TransactionalTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
import storm.trident.TridentState;
import storm.trident.TridentTopology;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.RawMultiScheme;
import backtype.storm.tuple.Fields;

public class TridentClassifiedEventsTopology {
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public static StormTopology buildTopology(Map<String, Object> conf) {
		TridentKafkaConfig spoutConfig = new TridentKafkaConfig(
				StaticHosts.fromHostString(
						Arrays.asList(new String[] { "localhost" }), 1), "test");
		spoutConfig.scheme = new RawMultiScheme();
		//FixedBatchSpout spout = TestSpoutFactory.build();
		TridentTopology topology = new TridentTopology();
		TransactionalTridentKafkaSpout spout = new TransactionalTridentKafkaSpout(spoutConfig);
		topology
				.newStream("spout1", spout)
				.parallelismHint(2)
				.each(new Fields("bytes"), new ParseJSON(), new Fields("description", "timestamp", "classifications"))
				.partitionPersist(new EventStoreStateFactory() , new Fields("description", "timestamp", "classifications"), new EventStoreUpdater());
		
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