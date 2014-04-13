package org.classified_event_aggregation.storm_input_topology.persistence;

import java.util.Map;

import backtype.storm.task.IMetricsContext;
import storm.trident.state.State;
import storm.trident.state.StateFactory;

@SuppressWarnings("serial")
public class NotificationStoreStateFactory implements StateFactory {
	
	@SuppressWarnings("rawtypes")
	@Override
	public State makeState(Map conf, IMetricsContext metrics, int partitionIndex, int numPartitions) {
		NotificationStore.Config config = new NotificationStore.Config();
		config.db = "notification";
		config.host = "localhost";
		config.password = "notification";
		config.user = "notification";
		return new NotificationStore(config);
	}

}
