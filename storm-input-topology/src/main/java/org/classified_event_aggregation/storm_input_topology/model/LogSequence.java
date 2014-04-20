package org.classified_event_aggregation.storm_input_topology.model;

import java.util.List;

public class LogSequence {

	private final String applicationName;
	private final String sequenceName;
	private final String sequenceId;
	private final List<LogMessage> logMessages;
	
	public LogSequence(String applicationName, String sequenceName, String sequenceId, List<LogMessage> logMessages) {
		this.applicationName = applicationName;
		this.sequenceName = sequenceName;
		this.sequenceId = sequenceId;
		this.logMessages = logMessages;
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

	public List<LogMessage> getLogMessages() {
		return logMessages;
	}

}
