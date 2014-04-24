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
	private ScheduledExecutorService timerLogger;
	private ScheduledExecutorService timerErrorLogger;

	public void run() {
		logBeforeStart();
		logger.info("Executing task");
		try {
			Thread.sleep(100L);
		} catch (InterruptedException e) {
			// ignore
		}
		timerLogger = Executors.newScheduledThreadPool(1);
		timerLogger.scheduleAtFixedRate(new Runnable() {
			public void run() {
				occasionallyLogError();
			}
		}, 0, 10, TimeUnit.MILLISECONDS);
		
		logAfterStart();
	}

	private void logBeforeStart() {
		MDC.put("SEQUENCE_NAME", UnstableSleepTask.class.getSimpleName());
		MDC.put("SEQUENCE_ID", UUID.randomUUID().toString());
		logger.info("Starting task #SEQUENCE_STATUS:STARTED");

		timerErrorLogger = Executors.newScheduledThreadPool(1);
		timerErrorLogger.scheduleAtFixedRate(new LogTask(logger), 0, 10, TimeUnit.MILLISECONDS);
	}

	private void logAfterStart() {
		timerErrorLogger.shutdown();
		try {
			timerErrorLogger.awaitTermination(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.error("Failed to interrupt logtask", e);
		}
		timerLogger.shutdown();
		try {
			timerLogger.awaitTermination(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.error("Failed to interrupt logtask", e);
		}
		logger.info("Finishing task #SEQUENCE_STATUS:FINISHED");
		MDC.clear();
	}
	
	private void occasionallyLogError(){
		int random = RandomUtils.nextInt(1, 25);
		if(random == 1)
			logger.error("Something went wrong");
	}

}
