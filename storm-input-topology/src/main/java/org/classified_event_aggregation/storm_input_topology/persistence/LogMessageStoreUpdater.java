package org.classified_event_aggregation.storm_input_topology.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.classified_event_aggregation.storm_input_topology.LogMessagesAnomalyDetectionTopology;
import org.classified_event_aggregation.storm_input_topology.model.LogMessage;
import org.classified_event_aggregation.storm_input_topology.model.LogSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.tuple.Values;
import storm.trident.operation.TridentCollector;
import storm.trident.state.BaseStateUpdater;
import storm.trident.tuple.TridentTuple;

@SuppressWarnings("serial")
public class LogMessageStoreUpdater extends BaseStateUpdater<LogMessageStore>{

	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(LogMessageStoreUpdater.class);

	private Map<String, LogSequence> logSequencesById = new HashMap<>();

	@Override
	public void updateState(LogMessageStore state, List<TridentTuple> tuples, TridentCollector collector) {
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

			if(logMessage.getClassifications().containsKey("SEQUENCE_STATUS") && logMessage.getClassifications().get("SEQUENCE_STATUS").getValue().contentEquals("FINISHED")){
				collector.emit(new Values(logSequence));
				log.debug("Emitting completed logsequence");
				logSequencesById.remove(sequenceId);
			}

		}

	}

}
