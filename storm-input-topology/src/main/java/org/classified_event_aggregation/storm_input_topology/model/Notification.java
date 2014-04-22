package org.classified_event_aggregation.storm_input_topology.model;

public class Notification {

	private String description; 
	private double relevance;
	private long timestamp;
	private String algorithmName;
	private LogSequence logSequence;
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getRelevance() {
		return relevance;
	}

	public void setRelevance(double relevance) {
		this.relevance = relevance;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getAlgorithmName() {
		return algorithmName;
	}

	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	public LogSequence getLogSequence() {
		return logSequence;
	}

	public void setLogSequence(LogSequence logSequence) {
		this.logSequence = logSequence;
	}
}
