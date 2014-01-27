package org.classified_event_aggregation.storm_input_topology.persistence;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.classified_event_aggregation.storm_input_topology.model.Classification;
import org.classified_event_aggregation.storm_input_topology.model.ClassifiedEvent;
import org.classified_event_aggregation.storm_input_topology.model.Event;

import storm.trident.operation.TridentCollector;
import storm.trident.state.BaseStateUpdater;
import storm.trident.tuple.TridentTuple;

@SuppressWarnings("serial")
public class EventStoreUpdater extends BaseStateUpdater<EventStore>{

	@Override
	public void updateState(EventStore state, List<TridentTuple> tuples, TridentCollector collector) {
		for (TridentTuple tridentTuple : tuples) {
			String description = tridentTuple.getStringByField("description");
			Long timestamp = tridentTuple.getLongByField("timestamp");
			Collection<String> classifications = (Collection<String>) tridentTuple.getValueByField("classifications");
			ClassifiedEvent ce = tupleToClassifiedEvent(description, timestamp, classifications);
			for (Classification classification : ce.getClassifications()) {
				state.incrementClassificationCounter(classification, timestamp, 1L);
			}
			state.storeClassifiedEvent(ce);
		}
	}
	
	/**
	 * @todo Move to util method (domain service ddd)
	 * 
	 * @param description
	 * @param timestamp
	 * @param classifications
	 * @return
	 */
	public static ClassifiedEvent tupleToClassifiedEvent(String description, Long timestamp, Collection<String> classifications){
		Event event = new Event(description, timestamp);
		
		// Parse classifications
		Set<Classification> classificationObjects = new HashSet<Classification>();
		for (String classification : classifications) {
			classificationObjects.add(Classification.fromString(classification));
		}
		
		return new ClassifiedEvent(event, classificationObjects);
	}

}
