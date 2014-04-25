package org.classified_event_aggregation.domain;

import java.util.List;

public class Application {
	private final String name;
	private final List<LogSequence> sequences;
	
	public Application(String name, List<LogSequence> sequences) {
		this.name = name;
		this.sequences = sequences;
	}
	
	public String getName() {
		return name;
	}

	public List<LogSequence> getSequences() {
		return sequences;
	}
}
