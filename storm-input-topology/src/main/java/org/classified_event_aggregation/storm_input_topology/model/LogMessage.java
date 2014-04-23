package org.classified_event_aggregation.storm_input_topology.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LogMessage {

	private final String description;
	private final String applicationName;
	private final Long timestamp;
	private final Map<String, Classification> classifications;

	public LogMessage(String applicationName, String description, Long timestamp) {
		this.applicationName = applicationName;
		this.description = description;
		this.timestamp = timestamp;
		this.classifications = Classification.fromString(description);
	}
	
	public String getApplicationName() {
		return applicationName;
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
		job.addProperty("timestamp", timestamp);
		job.addProperty("description", description);
		return job;
	}
	
	public static LogMessage fromJSON(JsonObject job){
		return new LogMessage(
			job.getAsJsonPrimitive("applicationName").getAsString(),
			job.getAsJsonPrimitive("description").getAsString(),
			job.getAsJsonPrimitive("timestamp").getAsLong()
		);
	}

	public static JsonArray toJson(List<LogMessage> messages){
		JsonArray jsonArray = new JsonArray();
		for (LogMessage message : messages) {
			jsonArray.add(message.toJSON());
		}
		return jsonArray;
	}
	
	public static List<LogMessage> fromJson(JsonArray jsonArray){
		List<LogMessage> logMessages = new ArrayList<>();
		for (JsonElement element : jsonArray) {
			logMessages.add(fromJSON(element.getAsJsonObject()));
		}
		return logMessages;
	}

}