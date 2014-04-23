package org.classified_event_aggregation.dummy_application.tasks;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


public class CPUIntensiveTask implements Runnable {

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(CPUIntensiveTask.class);
	private ScheduledExecutorService timer;
	
	public void run() {
		logBeforeStart();
		logger.info("Executing task");
		for (int i = 0; i < 10000; i++) {
			isPrime(i);
		}
		logAfterStart();
	}

	public boolean isPrime(int value) {
		boolean isPrime = true;

		for (int i = 2; isPrime && i < value; i++) {
			if (value % i == 0) {
				isPrime = false;
			}
		}

		return isPrime;
	}

	private void logBeforeStart() {
		MDC.put("SEQUENCE_NAME", CPUIntensiveTask.class.getSimpleName());
		MDC.put("SEQUENCE_ID", UUID.randomUUID().toString());
		logger.info("Starting task #SEQUENCE_STATUS:STARTED");

		timer = Executors.newScheduledThreadPool(1);
		timer.scheduleAtFixedRate(new LogTask(logger), 0, 150, TimeUnit.MILLISECONDS);
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

}
