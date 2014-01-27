package org.classified_event_aggregation.storm_input_topology.persistence;

import org.classified_event_aggregation.storm_input_topology.model.Classification;
import org.classified_event_aggregation.storm_input_topology.model.ClassifiedEvent;

import storm.trident.state.State;

public interface EventStore extends State {

	public void beginCommit(Long txid);

	public void commit(Long txid);

	public void storeClassifiedEvent(ClassifiedEvent event);

	void incrementClassificationCounter(Classification classification, Long timestamp, Long amount);
}
