package org.classified_event_aggregation.model;

import com.google.gson.JsonObject;

public class LogSequenceStatistics {

	private final String applicationName;
	private final String algorithmName;
	private final String sequenceName;
	private final String sequenceId;
	private final long startTimestamp;
	private final long endTimestamp;
	private final JsonObject statistics;
	
	public LogSequenceStatistics(String applicationName, String algorithmName, String sequenceName, String sequenceId, long startTimestamp, long endTimestamp, JsonObject statistics) {
		this.applicationName = applicationName;
		this.sequenceName = sequenceName;
		this.sequenceId = sequenceId;
		this.algorithmName = algorithmName;
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
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

	public long getStartTimestamp() {
		return startTimestamp;
	}
	
	public long getEndTimestamp() {
		return endTimestamp;
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
			job.getAsJsonPrimitive("startTimestamp").getAsLong(),
			job.getAsJsonPrimitive("endTimestamp").getAsLong(),
			job.getAsJsonPrimitive("statistics").getAsJsonObject()
		);
	}

	public JsonObject toJSON() {
		JsonObject job = new JsonObject();
		job.addProperty("sequenceName", sequenceName);
		job.addProperty("sequenceId", sequenceId);
		job.addProperty("startTimestamp", startTimestamp);
		job.addProperty("endTimestamp", endTimestamp);
		job.addProperty("applicationName", applicationName);
		job.addProperty("algorithmName", algorithmName);
		job.add("statistics", statistics);
		return job;
	}

}
