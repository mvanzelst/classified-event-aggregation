package org.classified_event_aggregation.storm_input_topology.persistence;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.classified_event_aggregation.storm_input_topology.model.Classification;
import org.classified_event_aggregation.storm_input_topology.model.ClassifiedEvent;
import org.classified_event_aggregation.storm_input_topology.model.Event;
import org.classified_event_aggregation.storm_input_topology.model.time.TimePeriod;

import storm.trident.operation.TridentCollector;
import storm.trident.state.BaseStateUpdater;
import storm.trident.tuple.TridentTuple;

@SuppressWarnings("serial")
public class EventStoreUpdater extends BaseStateUpdater<EventStore>{

	
	// @todo make configurable
	@SuppressWarnings("unchecked")
	public Collection<TimePeriod> timePeriods = 
		Arrays.asList(new TimePeriod[]{
			TimePeriod.YEAR,
			TimePeriod.MONTH,
			TimePeriod.WEEK,
			TimePeriod.DATE,
			TimePeriod.HOUR,
			TimePeriod.MINUTE,
			TimePeriod.SECOND,
		});
	
	@Override
	public void updateState(EventStore state, List<TridentTuple> tuples, TridentCollector collector) {
		for (TridentTuple tridentTuple : tuples) {
			String description = tridentTuple.getStringByField("description");
			Long timestamp = tridentTuple.getLongByField("timestamp");
			Collection<String> classifications = (Collection<String>) tridentTuple.getValueByField("classifications");
			ClassifiedEvent ce = tupleToClassifiedEvent(description, timestamp, classifications);
			for (Classification classification : ce.getClassifications()) {
				for (TimePeriod timePeriod : timePeriods) {
					long currentValue = state.getClassificationCounter(timePeriod.getName(), timePeriod.convertToStartOfPeriod(timestamp), classification);
					state.setClassificationCounter(timePeriod.getName(), timePeriod.convertToStartOfPeriod(timestamp), ce.toString(), classification, 1L + currentValue);
				}
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
			//classificationObjects.add(Classification.fromString(classification));
		}
		
		return new ClassifiedEvent(event, classificationObjects);
	}

}
