package org.classified_event_aggregation.storm_input_topology.model;

import com.google.gson.JsonObject;

public class LogSequenceStatistics {

	private final String applicationName;
	private final String algorithmName;
	private final String sequenceName;
	private final String sequenceId;
	private final long startTimestamp;
	private final long endTimestamp;
	private final JsonObject statistics;
	
	public LogSequenceStatistics(LogSequence logSequence, String algorithmName, JsonObject statistics) {
		this.applicationName = logSequence.getApplicationName();
		this.sequenceName = logSequence.getSequenceName();
		this.sequenceId = logSequence.getSequenceId();
		this.algorithmName = algorithmName;
		this.startTimestamp = logSequence.getStartTimestamp();
		this.endTimestamp = logSequence.getEndTimestamp();
		this.statistics = statistics;
	}
	
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
