package org.classified_event_aggregation.domain;

import java.util.List;

import com.google.gson.JsonObject;

public class LogSequence {

	private final String applicationName;
	private final String sequenceName;
	private final String sequenceId;
	private final long startTimestamp;
	private final long endTimestamp;
	private final List<LogMessage> logMessages;
	
	public LogSequence(String applicationName, String sequenceName, String sequenceId, long startTimestamp, long endTimestamp, List<LogMessage> logMessages) {
		this.applicationName = applicationName;
		this.sequenceName = sequenceName;
		this.sequenceId = sequenceId;
		this.logMessages = logMessages;
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
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
	
	public long getStartTimestamp() {
		return startTimestamp;
	}
	
	public long getEndTimestamp() {
		return endTimestamp;
	}

	public List<LogMessage> getLogMessages() {
		return logMessages;
	}

	public JsonObject toJSON() {
		JsonObject job = new JsonObject();
		job.addProperty("applicationName", applicationName);
		job.addProperty("sequenceName", sequenceName);
		job.addProperty("sequenceId", sequenceId);
		job.addProperty("startTimestamp", startTimestamp);
		job.addProperty("endTimestamp", endTimestamp);
		job.add("logMessages", LogMessage.toJson(logMessages));
		return job;
	}

}
