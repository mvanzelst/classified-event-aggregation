package org.classified_event_aggregation.storm_input_topology.model;

import java.io.Serializable;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class LogMessage implements Serializable {

	private final String description;
	private final Long timestamp;
	private final Map<String, Classification> classifications;

	public LogMessage(String description, Long timestamp) {
		this.description = description;
		this.timestamp = timestamp;
		this.classifications = Classification.fromString(description);
	}

	public String getDescription() {
		return description;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public Map<String, Classification> getClassifications() {
		return classifications;
	}
	
	public JsonObject toJSON(){
		JsonObject job = new JsonObject();
		job.add("timestamp", new JsonPrimitive(timestamp));
		job.add("description", new JsonPrimitive(description));
		return job;
	}
	
	public static LogMessage fromJSON(JsonObject job){
		return new LogMessage(
			job.getAsJsonPrimitive("description").getAsString(), 
			job.getAsJsonPrimitive("timestamp").getAsLong()
		);
	}

}