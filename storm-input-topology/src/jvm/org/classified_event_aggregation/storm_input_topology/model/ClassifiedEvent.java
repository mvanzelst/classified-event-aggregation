package org.classified_event_aggregation.storm_input_topology.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.collections.Sets;

public class ClassifiedEvent {
	
	public Event getEvent() {
		return event;
	}

	public Set<Classification> getClassifications() {
		return classifications;
	}

	public ClassifiedEvent(Event event, Set<Classification> classifications) {
		this.event = event;
		this.classifications = classifications;
	}
	
	private final Event event;
	private final Set<Classification> classifications;
	
	/**
	 * Convert a ClassifiedEvent into a collection off derived events
	 * A derived event contains all of but one of the classifications appended to the description
	 * The one classification that is not appended is to the description is the only classification added to the derived event
	 * @return
	 */
	public Collection<ClassifiedEvent> getDerivedEvents(){
		Collection<ClassifiedEvent> derivedEvents = new ArrayList<>();
		for (Classification classification : classifications) {
			Event derivedEvent = event;
			for (Classification classification2 : classifications) {
				if(classification2.equals(classification))
					continue;
				derivedEvent = derivedEvent.addClassificationToDescription(classification2);
			}
			derivedEvents.add(
				new ClassifiedEvent(
					derivedEvent, 
					new HashSet<Classification>(Arrays.asList(classification))
				)
			);
		}
		return derivedEvents;
	}
}
