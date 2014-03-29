package org.classified_event_aggregation.storm_input_topology.persistence;

import org.classified_event_aggregation.storm_input_topology.model.Classification;

public abstract class EventStoreDefault implements EventStore {
	@Override
	public void incrementClassificationCounter(String periodTypeName,
			Long periodStart, String lastDescription,
			Classification classification, Long amount) {
		long currentValue = getClassificationCounter(periodTypeName, periodStart, classification);
		setClassificationCounter(periodTypeName, periodStart, lastDescription, classification, currentValue + amount);
	}
}
