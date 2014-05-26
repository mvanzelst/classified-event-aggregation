package org.classified_event_aggregation.storm_input_topology.persistence;

import java.util.List;

import org.classified_event_aggregation.storm_input_topology.LogMessagesAnomalyDetectionTopologyBuilder;
import org.classified_event_aggregation.storm_input_topology.model.LogSequenceStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.trident.operation.TridentCollector;
import storm.trident.state.BaseStateUpdater;
import storm.trident.tuple.TridentTuple;

@SuppressWarnings("serial")
public class LogSequenceStatisticsStoreUpdater extends BaseStateUpdater<LogSequenceStatisticsStore>{

	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(LogSequenceStatisticsStoreUpdater.class);

	@Override
	public void updateState(LogSequenceStatisticsStore state, List<TridentTuple> tuples, TridentCollector collector) {		
		for (TridentTuple tridentTuple : tuples) {
			LogSequenceStatistics logSequenceStatistics = (LogSequenceStatistics) tridentTuple.getValueByField("log_sequence_statistics");
			state.storeLogSequenceStatistics(logSequenceStatistics);
		}
	}

}
