package org.classified_event_aggregation.storm_input_topology.persistence;

import java.util.List;

import storm.trident.operation.TridentCollector;
import storm.trident.state.BaseStateUpdater;
import storm.trident.tuple.TridentTuple;

@SuppressWarnings("serial")
public class TaskDurationUpdater extends BaseStateUpdater<TaskDurationStore>{

	@Override
	public void updateState(TaskDurationStore state, List<TridentTuple> tuples, TridentCollector collector) {
		for (TridentTuple tridentTuple : tuples) {
			state.storeDuration(
				tridentTuple.getStringByField("task_name"),
				tridentTuple.getLongByField("duration"),
				tridentTuple.getLongByField("start_timestamp"),
				tridentTuple.getLongByField("end_timestamp"),
				tridentTuple.getStringByField("task_id")
			);
		}
	}

}
