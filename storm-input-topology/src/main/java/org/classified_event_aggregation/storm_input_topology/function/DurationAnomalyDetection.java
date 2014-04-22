package org.classified_event_aggregation.storm_input_topology.function;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.classified_event_aggregation.storm_input_topology.model.Classification;
import org.classified_event_aggregation.storm_input_topology.model.LogMessage;
import org.classified_event_aggregation.storm_input_topology.model.LogSequence;
import org.classified_event_aggregation.storm_input_topology.model.LogSequenceStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.TridentOperationContext;
import storm.trident.tuple.TridentTuple;
import backtype.storm.tuple.Values;

import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;

@SuppressWarnings("serial")
public class DurationAnomalyDetection extends BaseFunction {
	
	private Long sample_size_min = null;
	private Long sample_size_max = null;
	
	@Override
	public void prepare(Map conf, TridentOperationContext context) {
		super.prepare(conf, context);
		sample_size_min = (Long) conf.get("algorithm.duration.sample_size.min");
		sample_size_max = (Long) conf.get("algorithm.duration.sample_size.min");
	}
	
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(DurationAnomalyDetection.class);

	// @TODO this could become very large
	// List for each unique sequenceName with a history of the amount of exceptions
	private Map<String, DescriptiveStatistics> sequenceDurationsMap = new HashMap<>();

	@Override
	public void execute(TridentTuple tuple, TridentCollector collector) {
		LogSequence logSequence = (LogSequence) tuple.getValueByField("log_sequence");
		DescriptiveStatistics sequenceDurations;
		if(sequenceDurationsMap.containsKey(logSequence.getSequenceName())){
			sequenceDurations = sequenceDurationsMap.get(logSequence.getSequenceName());
		} else {
			sequenceDurations = new DescriptiveStatistics(sample_size_max.intValue());
		}

		Long duration = getLogSequenceDuration(logSequence);
		if(duration == null){
			log.warn("Could not find duration of logsequence: {}", logSequence);
			return;
		}
		

		// The timestamp of the last logmessage
		long timestamp = Iterables.getLast(logSequence.getLogMessages()).getTimestamp();
		
		if(sequenceDurations.getN() >= sample_size_min){
			JsonObject stats = new JsonObject();
			stats.addProperty("duration", duration);
			stats.addProperty("standard_deviation", sequenceDurations.getStandardDeviation());
			stats.addProperty("variance", sequenceDurations.getVariance());
			stats.addProperty("max", sequenceDurations.getMax());
			stats.addProperty("min", sequenceDurations.getMax());
			stats.addProperty("mean", sequenceDurations.getMean());
			stats.addProperty("sample_size", sequenceDurations.getN());
			stats.addProperty("skewness", sequenceDurations.getSkewness());
			LogSequenceStatistics logSequenceStatistics = new LogSequenceStatistics(logSequence, "duration_statistics", stats);
			collector.emit(new Values(logSequenceStatistics));
		}

		// Store the numExceptions of the current LogSequence
		sequenceDurations.addValue(duration);

		// Store the list
		sequenceDurationsMap.put(logSequence.getSequenceName(), sequenceDurations);
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
