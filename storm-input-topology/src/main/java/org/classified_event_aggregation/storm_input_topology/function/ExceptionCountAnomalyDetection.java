package org.classified_event_aggregation.storm_input_topology.function;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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
public class ExceptionCountAnomalyDetection extends BaseFunction {
	
	private Long sample_size_min = null;
	private Long sample_size_max = null;
	
	@Override
	public void prepare(Map conf, TridentOperationContext context) {
		super.prepare(conf, context);
		sample_size_min = (Long) conf.get("algorithm.exception_count.sample_size.min");
		sample_size_max = (Long) conf.get("algorithm.exception_count.sample_size.max");
	}
	
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(ExceptionCountAnomalyDetection.class);

	// @TODO this could become very large
	// List for each unique sequenceName with a history of the amount of exceptions
	private Map<String, DescriptiveStatistics> sequenceNumExceptionsMap = new HashMap<>();

	@Override
	public void execute(TridentTuple tuple, TridentCollector collector) {
		LogSequence logSequence = (LogSequence) tuple.getValueByField("log_sequence");
		DescriptiveStatistics sequenceNumExceptions;
		if(sequenceNumExceptionsMap.containsKey(logSequence.getSequenceName())){
			sequenceNumExceptions = sequenceNumExceptionsMap.get(logSequence.getSequenceName());
		} else {
			sequenceNumExceptions = new DescriptiveStatistics(sample_size_max.intValue());
		}

		int numExceptions = countExceptions(logSequence);

		// The timestamp of the last logmessage
		long timestamp = Iterables.getLast(logSequence.getLogMessages()).getTimestamp();
		
		if(sequenceNumExceptions.getN() >= sample_size_min){
			JsonObject stats = new JsonObject();
			stats.addProperty("num_exceptions", numExceptions);
			stats.addProperty("standard_deviation", sequenceNumExceptions.getStandardDeviation());
			stats.addProperty("variance", sequenceNumExceptions.getVariance());
			stats.addProperty("max", sequenceNumExceptions.getMax());
			stats.addProperty("min", sequenceNumExceptions.getMax());
			stats.addProperty("mean", sequenceNumExceptions.getMean());
			stats.addProperty("sample_size", sequenceNumExceptions.getN());
			stats.addProperty("skewness", sequenceNumExceptions.getSkewness());
			LogSequenceStatistics logSequenceStatistics = new LogSequenceStatistics(logSequence, "num_exceptions_statistics", stats);
			collector.emit(new Values(logSequenceStatistics));
		}

		// Store the numExceptions of the current LogSequence
		sequenceNumExceptions.addValue(numExceptions);

		// Store the list
		sequenceNumExceptionsMap.put(logSequence.getSequenceName(), sequenceNumExceptions);
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
