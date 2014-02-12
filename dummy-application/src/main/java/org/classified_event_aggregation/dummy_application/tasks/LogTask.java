package org.classified_event_aggregation.dummy_application.tasks;

import org.slf4j.Logger;

public class LogTask implements Runnable {

	private final Logger logger;
	private int counter = 0;
	
	public LogTask(Logger logger) {
		this.logger = logger;
	}
	
	public void run() {
		logger.info("Emitting log message, sequence:" + this.counter++);
	}

}
