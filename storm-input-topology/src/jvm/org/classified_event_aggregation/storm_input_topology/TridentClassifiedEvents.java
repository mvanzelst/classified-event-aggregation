package org.classified_event_aggregation.storm_input_topology;

import storm.kafka.BrokerHosts;
import storm.kafka.HostPort;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StaticHosts;
import storm.kafka.trident.GlobalPartitionInformation;
import storm.trident.TridentState;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.builtin.Count;
import storm.trident.operation.builtin.FilterNull;
import storm.trident.operation.builtin.MapGet;
import storm.trident.operation.builtin.Sum;
import storm.trident.testing.MemoryMapState;
import storm.trident.tuple.TridentTuple;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.LocalDRPC;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class TridentClassifiedEvents {
	public static class Split extends BaseFunction {
		@Override
		public void execute(TridentTuple tuple, TridentCollector collector) {
			String sentence = tuple.getString(0);
			for (String word : sentence.split(" ")) {
				collector.emit(new Values(word));
			}
		}
	}

	public static StormTopology buildTopology(LocalDRPC drpc) {
		GlobalPartitionInformation hostsAndPartitions = new GlobalPartitionInformation();
		hostsAndPartitions.addPartition(0, new HostPort("localhost", 9092));
		BrokerHosts brokerHosts = new StaticHosts(hostsAndPartitions);
		SpoutConfig spoutConfig = new SpoutConfig(brokerHosts, // list of Kafka
																// brokers
				"test", // number of partitions per host
				"/kafkastorm", // the root path in Zookeeper for the spout to
								// store the consumer offsets
				"discovery"); // an id for this consumer for storing the
								// consumer offsets in Zookeeper
		KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);

		TridentTopology topology = new TridentTopology();
		TridentState wordCounts = topology
				.newStream("spout1", kafkaSpout)
				.parallelismHint(16)
				.each(new Fields("sentence"), new Split(), new Fields("word"))
				.groupBy(new Fields("word"))
				.persistentAggregate(new MemoryMapState.Factory(), new Count(),
						new Fields("count")).parallelismHint(16);

		topology.newDRPCStream("words", drpc)
				.each(new Fields("args"), new Split(), new Fields("word"))
				.groupBy(new Fields("word"))
				.stateQuery(wordCounts, new Fields("word"), new MapGet(),
						new Fields("count"))
				.each(new Fields("count"), new FilterNull())
				.aggregate(new Fields("count"), new Sum(), new Fields("sum"));
		return topology.build();
	}

	public static void main(String[] args) throws Exception {
		Config conf = new Config();
		conf.setMaxSpoutPending(20);
		if (args.length == 0) {
			LocalDRPC drpc = new LocalDRPC();
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("wordCounter", conf, buildTopology(drpc));
			for (int i = 0; i < 100; i++) {
				System.out.println("DRPC RESULT: "
						+ drpc.execute("words", "cat the dog jumped"));
				Thread.sleep(1000);
			}
		} else {
			conf.setNumWorkers(3);
			StormSubmitter.submitTopology(args[0], conf, buildTopology(null));
		}
	}
}