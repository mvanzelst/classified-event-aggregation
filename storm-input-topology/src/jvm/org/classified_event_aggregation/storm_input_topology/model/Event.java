package org.classified_event_aggregation.storm_input_topology.model;

public class Event {
	
	public Event(String description, Long timestamp) {
		this.description = description;
		this.timestamp = timestamp;
	}

	private final String description;
	private final Long timestamp;
	
	public Event addClassificationToDescription(Classification classification){
		return new Event(
			description + " " + classification, 
			timestamp
		);
	}

	public String getDescription() {
		return description;
	}

	public Long getTimestamp() {
		return timestamp;
	}

}
