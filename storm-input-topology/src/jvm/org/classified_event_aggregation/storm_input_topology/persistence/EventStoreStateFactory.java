package org.classified_event_aggregation.storm_input_topology.persistence;

import java.util.Map;

import backtype.storm.task.IMetricsContext;
import storm.trident.state.State;
import storm.trident.state.StateFactory;

@SuppressWarnings("serial")
public class EventStoreStateFactory implements StateFactory {

	@SuppressWarnings("rawtypes")
	@Override
	public State makeState(Map conf, IMetricsContext metrics, int partitionIndex, int numPartitions) {
		if("cassandra".contentEquals((String) conf.get("databaseType"))){
			// @todo make configurable
			return new CassandraEventStore();
		} else {
			throw new RuntimeException("Unsupported database type");
		}
	}

}
