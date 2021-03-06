package org.classified_event_aggregation.storm_input_topology.persistence;

import java.util.Map;

import backtype.storm.task.IMetricsContext;
import storm.trident.state.State;
import storm.trident.state.StateFactory;

@SuppressWarnings("serial")
public class LogMessageStoreStateFactory implements StateFactory {

	@SuppressWarnings("rawtypes")
	@Override
	public State makeState(Map conf, IMetricsContext metrics, int partitionIndex, int numPartitions) {
		LogMessageStore.Config config = new LogMessageStore.Config();
		config.keySpace = "cea";
		config.node = "localhost";
		return new LogMessageStore(config);
	}

}
