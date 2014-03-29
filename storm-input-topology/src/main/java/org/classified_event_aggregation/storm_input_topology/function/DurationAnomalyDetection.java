package org.classified_event_aggregation.storm_input_topology.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.classified_event_aggregation.storm_input_topology.LogMessagesAnomalyDetectionTopology;
import org.classified_event_aggregation.storm_input_topology.model.Classification;
import org.classified_event_aggregation.storm_input_topology.model.LogMessage;
import org.classified_event_aggregation.storm_input_topology.model.LogSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;
import backtype.storm.tuple.Values;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Doubles;

public class DurationAnomalyDetection extends BaseFunction {
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(DurationAnomalyDetection.class);

	// @TODO this could become very large
	// List for each unique sequenceName with a history of the amount of exceptions
	private Map<String, List<Long>> sequenceDurationsMap = new HashMap<>();

	@Override
	public void execute(TridentTuple tuple, TridentCollector collector) {
		LogSequence logSequence = (LogSequence) tuple.getValueByField("log_sequence");
		List<Long> sequenceDurations;
		if(sequenceDurationsMap.containsKey(logSequence.getSequenceName())){
			sequenceDurations = sequenceDurationsMap.get(logSequence.getSequenceName());
		} else {
			sequenceDurations = new ArrayList<>();
		}

		Long duration = getLogSequenceDuration(logSequence);
		if(duration == null){
			log.warn("Could not find duration of logsequence: {}", logSequence);
			return;
		}

		/*
		 * Output notification
		 */
		String algorithmName = "Log duration anomaly";
		String description;

		// The timestamp of the last logmessage
		long timestamp = Iterables.getLast(logSequence.getLogMessages()).getTimestamp();

		double relevance;

		if(aboveSixSigma(sequenceDurations, duration)){
			description = "The duration of the log sequence is above the sixth sigma";

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

		// Store the numExceptions of the current LogSequence
		sequenceDurations.add(duration);

		// Cap the list
		if(sequenceDurations.size() > 100)
			sequenceDurations.remove(0);

		// Store the list
		sequenceDurationsMap.put(logSequence.getSequenceName(), sequenceDurations);
	}

	private boolean aboveSixSigma(List<Long> stats, double number){
		DescriptiveStatistics dstats = new DescriptiveStatistics(Doubles.toArray(stats));
		double aboveMean = number - dstats.getMean();
		double aboveMeanInStdDev = aboveMean / dstats.getStandardDeviation();
		if(aboveMeanInStdDev >= 6){
			log.info("{} is above sixth sigma with sigma of {} ", number, aboveMeanInStdDev);
			return true;
		} else {
			log.debug("{} is below sixth sigma with sigma of {} ", number, aboveMeanInStdDev);
			return false;
		}
	}

	private Long getLogSequenceDuration(LogSequence logSequence){
		Long start = null, end = null;
		for (LogMessage logMessage : logSequence.getLogMessages()){
			Classification status = logMessage.getClassifications().get("SEQUENCE_STATUS");
			if(status != null){
				if(status.getValue().contentEquals("STARTED")){
					start = logMessage.getTimestamp();
				} else if(status.getValue().contentEquals("FINISHED")){
					end = logMessage.getTimestamp();
				}
			}
		}
		if(start == null || end == null)
			return null;

		return end - start;
	}

}
