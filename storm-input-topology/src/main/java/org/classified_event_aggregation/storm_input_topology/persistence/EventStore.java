package org.classified_event_aggregation.storm_input_topology.persistence;

import org.classified_event_aggregation.storm_input_topology.model.Classification;
import org.classified_event_aggregation.storm_input_topology.model.ClassifiedEvent;

import storm.trident.state.State;

public interface EventStore extends State {

	public void beginCommit(Long txid);

	public void commit(Long txid);

	public void storeClassifiedEvent(ClassifiedEvent event);

	public void incrementClassificationCounter(String periodTypeName, Long periodStart, String lastDescription, Classification classification, Long amount);

	public void setClassificationCounter(String periodTypeName, Long periodStart, String lastDescription, Classification classification, Long amount);

	public Long getClassificationCounter(String periodTypeName, Long periodStart, Classification classification);
}
