package org.classified_event_aggregation.dummy_application;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class Controller {
	private Executor executor;
	
	public void initiate(int numThreads){
		executor = Executors.newFixedThreadPool(numThreads);
	}
	
	public void execute(Runnable task){
		executor.execute(task);
	}
}
