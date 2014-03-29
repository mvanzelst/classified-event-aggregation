package org.classified_event_aggregation.dummy_application.tasks;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


public class UnstableSleepTask implements Runnable {

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(UnstableSleepTask.class);
	private ScheduledExecutorService timer;

	public void run() {
		logBeforeStart();
		logger.info("Executing task");
		try {
			Thread.sleep(100L);
		} catch (InterruptedException e) {
			// ignore
		}
		occasionallyLogError();
		logAfterStart();
	}

	private void logBeforeStart() {
		MDC.put("SEQUENCE_NAME", UnstableSleepTask.class.getSimpleName());
		MDC.put("SEQUENCE_ID", UUID.randomUUID().toString());
		logger.info("Starting task #SEQUENCE_STATUS:STARTED");

		timer = Executors.newScheduledThreadPool(1);
		timer.scheduleAtFixedRate(new LogTask(logger), 0, 10, TimeUnit.MILLISECONDS);
	}

	private void logAfterStart() {
		timer.shutdown();
		try {
			timer.awaitTermination(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.error("Failed to interrupt logtask", e);
		}
		logger.info("Finishing task #SEQUENCE_STATUS:FINISHED");
		MDC.clear();
	}
	
	private void occasionallyLogError(){
		// Random number in range 1-250 inclusive
		int random = RandomUtils.nextInt(1, 251);
		if(random == 1)
			logger.error("Something went wrong");
	}

}
