package org.classified_event_aggregation.storm_input_topology.model;

import java.util.List;

import com.google.gson.JsonObject;

public class LogSequence {

	private final String applicationName;
	private final String sequenceName;
	private final String sequenceId;
	private final long timestamp;
	private final List<LogMessage> logMessages;
	
	public LogSequence(String applicationName, String sequenceName, String sequenceId, long timestamp, List<LogMessage> logMessages) {
		this.applicationName = applicationName;
		this.sequenceName = sequenceName;
		this.sequenceId = sequenceId;
		this.logMessages = logMessages;
		this.timestamp = timestamp;
	}
	
	public String getApplicationName() {
		return applicationName;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public String getSequenceId() {
		return sequenceId;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public List<LogMessage> getLogMessages() {
		return logMessages;
	}

	public JsonObject toJSON() {
		JsonObject job = new JsonObject();
		job.addProperty("sequenceName", sequenceName);
		job.addProperty("sequenceId", sequenceId);
		job.addProperty("timestamp", timestamp);
		job.add("logMessages", LogMessage.toJson(logMessages));
		return job;
	}

}
