package org.classified_event_aggregation.model;

import com.google.gson.JsonObject;

public class LogSequenceStatistics {

	private final String applicationName;
	private final String algorithmName;
	private final String sequenceName;
	private final String sequenceId;
	private final long timestamp;
	private final JsonObject statistics;
	
	public LogSequenceStatistics(String applicationName, String algorithmName, String sequenceName, String sequenceId, long timestamp, JsonObject statistics) {
		this.applicationName = applicationName;
		this.sequenceName = sequenceName;
		this.sequenceId = sequenceId;
		this.algorithmName = algorithmName;
		this.timestamp = timestamp;
		this.statistics = statistics;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public String getSequenceId() {
		return sequenceId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getAlgorithmName() {
		return algorithmName;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public JsonObject getStatistics() {
		return statistics;
	}

	public static LogSequenceStatistics fromJSON(JsonObject job) {
		return new LogSequenceStatistics(
			job.getAsJsonPrimitive("applicationName").getAsString(),
			job.getAsJsonPrimitive("algorithmName").getAsString(),
			job.getAsJsonPrimitive("sequenceName").getAsString(),
			job.getAsJsonPrimitive("sequenceId").getAsString(),
			job.getAsJsonPrimitive("timestamp").getAsLong(),
			job.getAsJsonPrimitive("statistics").getAsJsonObject()
		);
	}

}
