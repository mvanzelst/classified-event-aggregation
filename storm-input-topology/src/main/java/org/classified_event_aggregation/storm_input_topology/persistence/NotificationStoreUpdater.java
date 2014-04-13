package org.classified_event_aggregation.storm_input_topology.persistence;

import java.util.List;

import org.classified_event_aggregation.storm_input_topology.model.LogSequence;
import org.classified_event_aggregation.storm_input_topology.model.Notification;

import storm.trident.operation.TridentCollector;
import storm.trident.state.BaseStateUpdater;
import storm.trident.tuple.TridentTuple;

@SuppressWarnings("serial")
public class NotificationStoreUpdater extends BaseStateUpdater<NotificationStore>{


	@Override
	public void updateState(NotificationStore state, List<TridentTuple> tuples, TridentCollector collector) {
		for (TridentTuple tuple : tuples) {
			Notification notification = new Notification();
			notification.setDescription(tuple.getStringByField("description"));
			notification.setRelevance(tuple.getDoubleByField("relevance"));
			notification.setTimestamp(tuple.getLongByField("timestamp"));
			notification.setAlgorithmName(tuple.getStringByField("algorithm_name"));
			notification.setLogSequence((LogSequence) tuple.getValueByField("log_sequence"));
			state.storeNotification(notification);
		}
	}

}
