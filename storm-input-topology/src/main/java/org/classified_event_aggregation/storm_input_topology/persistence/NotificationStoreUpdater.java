package org.classified_event_aggregation.storm_input_topology.persistence;

import java.util.List;

import storm.trident.operation.TridentCollector;
import storm.trident.state.BaseStateUpdater;
import storm.trident.tuple.TridentTuple;

@SuppressWarnings("serial")
public class NotificationStoreUpdater extends BaseStateUpdater<NotificationStore>{


	@Override
	public void updateState(NotificationStore state, List<TridentTuple> tuples, TridentCollector collector) {
		//state.storeNotification(description, relevance, timestamp, algorithmName);
	}

}
