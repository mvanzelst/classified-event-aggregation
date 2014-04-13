package org.classified_event_aggregation.storm_input_topology.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class LogSequence implements Serializable {

	private final String sequenceName;
	private final String sequenceId;
	private final List<LogMessage> logMessages;
	
	public LogSequence(String sequenceName, String sequenceId, List<LogMessage> logMessages) {
		this.sequenceName = sequenceName;
		this.sequenceId = sequenceId;
		this.logMessages = logMessages;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public String getSequenceId() {
		return sequenceId;
	}

	public List<LogMessage> getLogMessages() {
		return logMessages;
	}

	public JsonObject toJSON() {
		JsonObject job = new JsonObject();
		job.add("sequenceName", new JsonPrimitive(sequenceName));
		job.add("sequenceId", new JsonPrimitive(sequenceId));
		JsonArray arr = new JsonArray();
		for (LogMessage logMessage : logMessages) {
			arr.add(logMessage.toJSON());
		}
		job.add("logMessages", arr);
		return job;
	}
	
	public static LogSequence fromJSON(JsonObject job){
		return new LogSequence(
			job.getAsJsonPrimitive("sequenceName").getAsString(), 
			job.getAsJsonPrimitive("sequenceId").getAsString(), 
			toLogMessages(job.getAsJsonArray("logMessages"))
		);
	}
	
	private static List<LogMessage> toLogMessages(JsonArray logMessagesJson){
		List<LogMessage> logMessages = new ArrayList<>();
		for (JsonElement jsonElement : logMessagesJson) {
			logMessages.add(LogMessage.fromJSON(jsonElement.getAsJsonObject()));
		}
		return logMessages;
	}

}
