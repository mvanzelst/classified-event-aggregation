package org.classified_event_aggregation.dummy_application;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.classified_event_aggregation.dummy_application.tasks.TaskBuilder;

public class TaskInitiator {
	public static void main(String[] args) throws InterruptedException {
		Executor executor = Executors.newFixedThreadPool(4);

		List<TaskBuilder.TASKS> tasks = new ArrayList<TaskBuilder.TASKS>();
		tasks.add(TaskBuilder.TASKS.CPUINTENSIVETASK);

		for(int i=0; i<10000; i++){
			TaskBuilder.TASKS taskName = tasks.get(new Random().nextInt(tasks.size()));
			Runnable task = TaskBuilder.build(taskName);
			executor.execute(task);
		}
		System.out.println("done adding tasks");
	}
}