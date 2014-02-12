package org.classified_event_aggregation.dummy_application;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.classified_event_aggregation.dummy_application.tasks.TaskBuilder;

public class TaskInitiator {
	public static void main(String[] args) throws InterruptedException {
		Controller controller = new Controller();
		controller.initiate(4);

		List<TaskBuilder.TASKS> tasks = new ArrayList<TaskBuilder.TASKS>();
		tasks.add(TaskBuilder.TASKS.CPUINTENSIVETASK);
		for(int i=0; i<10000; i++){
			TaskBuilder.TASKS taskName = tasks.get(new Random().nextInt(tasks.size()));
			Runnable task = TaskBuilder.build(taskName);
			controller.execute(task);
		}
		System.out.println("done adding tasks");
	}
}
