package org.classified_event_aggregation.storm_input_topology.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.classified_event_aggregation.storm_input_topology.model.LogMessage;
import org.classified_event_aggregation.storm_input_topology.model.LogSequence;

import backtype.storm.tuple.Values;
import storm.trident.operation.TridentCollector;
import storm.trident.state.BaseStateUpdater;
import storm.trident.tuple.TridentTuple;

@SuppressWarnings("serial")
public class LogMessageStoreUpdater extends BaseStateUpdater<EventStore>{

	private Map<String, LogSequence> logSequencesById;

	@Override
	public void updateState(EventStore state, List<TridentTuple> tuples, TridentCollector collector) {
		for (TridentTuple tridentTuple : tuples) {

			LogMessage logMessage = (LogMessage) tridentTuple.getValueByField("log_message");
			String sequenceId = logMessage.getClassifications().get("SEQUENCE_ID").getValue();
			String sequenceName = logMessage.getClassifications().get("SEQUENCE_NAME").getValue();

			LogSequence logSequence = logSequencesById.get(sequenceId);
			if(logSequence == null){
				logSequence = new LogSequence(sequenceName, sequenceId, new ArrayList<LogMessage>());
			}

			logSequence.getLogMessages().add(logMessage);
			logSequencesById.put(sequenceId, logSequence);

			if(logMessage.getClassifications().containsKey("STATUS") && logMessage.getClassifications().get("STATUS").getValue().contentEquals("FINISHED")){
				collector.emit(new Values(logSequence));
				logSequencesById.remove(sequenceId);
			}

		}

	}

}
