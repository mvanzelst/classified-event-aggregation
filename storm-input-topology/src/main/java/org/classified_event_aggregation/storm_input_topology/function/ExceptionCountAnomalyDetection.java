package org.classified_event_aggregation.storm_input_topology.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.classified_event_aggregation.storm_input_topology.model.LogMessage;
import org.classified_event_aggregation.storm_input_topology.model.LogSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;
import backtype.storm.tuple.Values;

import com.google.common.collect.Iterables;

public class ExceptionCountAnomalyDetection extends BaseFunction {
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(ExceptionCountAnomalyDetection.class);

	// @TODO this could become very large
	// List for each unique sequenceName with a history of the amount of exceptions
	private Map<String, List<Integer>> sequenceExceptionsMap = new HashMap<>();

	@Override
	public void execute(TridentTuple tuple, TridentCollector collector) {
		LogSequence logSequence = (LogSequence) tuple.getValueByField("log_sequence");
		List<Integer> sequenceExceptions;
		if(sequenceExceptionsMap.containsKey(logSequence.getSequenceName())){
			sequenceExceptions = sequenceExceptionsMap.get(logSequence.getSequenceName());
		} else {
			sequenceExceptions = new ArrayList<>();
		}

		// Count the number of exceptions in the current log sequence
		int numExceptions = countExceptions(logSequence);

		int maxExceptions;

		// What is the maximum number of exceptions
		if(sequenceExceptions.size() > 0)
			maxExceptions = Collections.max(sequenceExceptions);
		else 
			maxExceptions = 0;

		/*
		 * Output notification
		 */
		String algorithmName = "Amount of exceptions anomaly";
		String description;

		// The timestamp of the last logmessage
		long timestamp = Iterables.getLast(logSequence.getLogMessages()).getTimestamp();

		double relevance;

		if(sequenceExceptions.size() >= 10){
			if(numExceptions > maxExceptions){
				description = "An anomalous exception occured";
	
				// @TODO the relevance could be based on couple of things:
				// The number of previous values
				// The standard deviation of the set
				// The change with respect to the stddev
				relevance = 1;
				log.debug(description);
			} else {
				description = "No anomalous exceptions occured";
				relevance = 0;
			}
			collector.emit(new Values(description, relevance, timestamp, algorithmName));
		}

		

		// Store the numExceptions of the current LogSequence
		sequenceExceptions.add(numExceptions);

		// Cap the list
		if(sequenceExceptions.size() > 100)
			sequenceExceptions.remove(0);

		// Store the list
		sequenceExceptionsMap.put(logSequence.getSequenceName(), sequenceExceptions);
	}

	private int countExceptions(LogSequence logSequence){
		int numExceptions = 0;
		for (LogMessage logMessage : logSequence.getLogMessages()) {
			if(logMessage.getClassifications().containsKey("LOG_LEVEL") && logMessage.getClassifications().get("LOG_LEVEL").getValue().contentEquals("ERROR"))
				numExceptions++;
		}
		return numExceptions;
	}

}
