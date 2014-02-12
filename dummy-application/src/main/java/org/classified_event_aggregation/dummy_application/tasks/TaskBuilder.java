package org.classified_event_aggregation.dummy_application.tasks;

public class TaskBuilder {

	public enum TASKS {
		CPUINTENSIVETASK
	}
	
	public static Runnable build(TASKS taskName){
		switch (taskName) {
			case CPUINTENSIVETASK:
				return new CPUIntensiveTask();
			default:
				throw new IllegalArgumentException("Unknown enum value");
		}
	}
}
