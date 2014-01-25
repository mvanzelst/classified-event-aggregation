package org.classified_event_aggregation.storm_input_topology;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

import org.classified_event_aggregation.storm_input_topology.persistence.CassandraEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.kafka.HostPort;
import storm.kafka.KafkaConfig.BrokerHosts;
import storm.kafka.KafkaConfig.StaticHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.RawScheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class TridentClassifiedEvents {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@SuppressWarnings("serial")
	public static class ParseJSON extends BaseFunction {
		private final Logger log = LoggerFactory.getLogger(getClass());
		
		@Override
		public final void execute(
			final TridentTuple tuple,
			final TridentCollector collector
		) {
			byte[] bytes = tuple.getBinary(0);
			String decoded = new String(bytes, Charset.forName("UTF-8"));
			if(log.isDebugEnabled()){
				log.debug("Received message: " + decoded);
			} else {
				log.info("Received message");
			}
			Gson gson = new Gson();
			Type type = new TypeToken<Map<String, String>>(){}.getType();
			Map<String, String> map;
			try {
				map = gson.fromJson(decoded, type);
			} catch (JsonSyntaxException e){
				log.error("Failed to parse json string: " + decoded, e);
				return;
			}
			collector.emit(new Values(
				map.get("description"),
				map.get("timestamp"),
				Arrays.asList(map.get("tags").split(" "))
			));
		}
	}

	public static StormTopology buildTopology(Config conf) {
		BrokerHosts brokerHosts = new StaticHosts(Arrays.asList(new HostPort("localhost", 9092)), 1);
		SpoutConfig spoutConfig = new SpoutConfig(
			brokerHosts, // list of Kafka
			"test", // topic
			"/kafkastorm", // the root path in Zookeeper for the spout to store the consumer offsets
			"discovery" // an id for this consumer for storing the consumer offsets in Zookeeper
		); 
		spoutConfig.scheme = new RawScheme();
		KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);
		TridentTopology topology = new TridentTopology();
		topology
				.newStream("spout1", kafkaSpout)
				.parallelismHint(1)
				.each(new Fields("bytes"), new ParseJSON(), new Fields("description", "timestamp", "tags"));
		return topology.build();
	}

	public static void main(String[] args) throws Exception {
		Config conf = new Config();
		conf.put("cassandra_event_store.config", new CassandraEventStore.Config());
		conf.setMaxSpoutPending(20);
		if (args.length == 0) {
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("classifiedEventProcessor", conf, buildTopology(conf));
		} else {
			conf.setNumWorkers(3);
			StormSubmitter.submitTopology(args[0], conf, buildTopology(conf));
		}
	}
}