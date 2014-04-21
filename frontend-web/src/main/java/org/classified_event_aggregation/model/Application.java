package org.classified_event_aggregation.model;

import java.util.List;

public class Application {
	private final String name;
	private final List<Task> tasks;
	
	public Application(String name, List<Task> tasks) {
		this.name = name;
		this.tasks = tasks;
	}
	
	public String getName() {
		return name;
	}

	public List<Task> getTasks() {
		return tasks;
	}
}
