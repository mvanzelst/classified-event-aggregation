package org.classified_event_aggregation.storm_input_topology.model;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class LogSequence {

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
		job.add("logmessages", arr);
		return job;
	}

}
