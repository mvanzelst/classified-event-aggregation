package org.classified_event_aggregation.test.topology;

import java.util.Map;

import org.classified_event_aggregation.storm_input_topology.persistence.NotificationStoreStateFactory;

import storm.trident.state.State;
import backtype.storm.task.IMetricsContext;

public class NotificationStoreStateFactoryMock extends NotificationStoreStateFactory {

	@Override
	public State makeState(Map conf, IMetricsContext metrics,
			int partitionIndex, int numPartitions) {
		// TODO Auto-generated method stub
		return super.makeState(conf, metrics, partitionIndex, numPartitions);
	}
}
