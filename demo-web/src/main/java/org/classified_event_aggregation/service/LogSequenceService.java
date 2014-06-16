package org.classified_event_aggregation.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.classified_event_aggregation.domain.Classification;
import org.classified_event_aggregation.domain.LogMessage;
import org.classified_event_aggregation.domain.LogSequence;
import org.classified_event_aggregation.persistence.LogMessageStore;
import org.classified_event_aggregation.persistence.LogSequenceStatisticsStore;
import org.classified_event_aggregation.domain.LogSequenceStatistics;
import org.springframework.stereotype.Service;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;

@Service
public class LogSequenceService {

	private LogSequenceStatisticsStore logSequenceStatisticsStore;
	private LogMessageStore logMessageStore;
	
	@PostConstruct
	public void setup(){
		Cluster cluster = Cluster.builder().addContactPoint("localhost").build();
		Session session = cluster.connect();
		session.execute("DROP KEYSPACE IF EXISTS cea_demo");
		session.execute(
			"CREATE KEYSPACE IF NOT EXISTS cea_demo " +
					"WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 3};"
		);
		session.shutdown();
		
		LogSequenceStatisticsStore.Config statisticsStoreConfig = new LogSequenceStatisticsStore.Config();
		statisticsStoreConfig.keySpace = "cea_demo";
		statisticsStoreConfig.node = "localhost";
		logSequenceStatisticsStore = new LogSequenceStatisticsStore(statisticsStoreConfig);

		LogMessageStore.Config logMessageStoreConfig = new LogMessageStore.Config();
		logMessageStoreConfig.keySpace = "cea_demo";
		logMessageStoreConfig.node = "localhost";
		logMessageStore = new LogMessageStore(logMessageStoreConfig);
	}
	
	public void store(List<LogMessage> logMessages) {
		LogMessage firstLogMessage = logMessages.get(0);
		String applicationName = firstLogMessage.getApplicationName();
		String sequenceName = firstLogMessage.getClassifications().get("SEQUENCE_NAME").getValue();
		String sequenceId = firstLogMessage.getClassifications().get("SEQUENCE_ID").getValue();
		
		LogMessage lastLogMessage = Iterables.getLast(logMessages);
		
		for (LogMessage logMessage : logMessages) {
			logMessageStore.storeLogMessage(logMessage.getDescription(), logMessage.getTimestamp(), sequenceName, sequenceId);
		}
		
		LogSequence logSequence = new LogSequence(
			applicationName, 
			sequenceName, 
			sequenceId, 
			firstLogMessage.getTimestamp(), 
			lastLogMessage.getTimestamp(), 
			logMessages
		);
		
		LogSequenceStatistics logSequenceStatistics = ExceptionStatisticsGatherer.execute(logSequence);
		if(logSequenceStatistics != null){
			logSequenceStatisticsStore.storeLogSequenceStatistics(logSequenceStatistics);
		}
		
		logSequenceStatistics = DurationStatisticsGatherer.execute(logSequence);
		if(logSequenceStatistics != null){
			logSequenceStatisticsStore.storeLogSequenceStatistics(logSequenceStatistics);
		}
	}
	
	private static class ExceptionStatisticsGatherer {
		
		private static Map<String, DescriptiveStatistics> sequenceNumExceptionsMap = new HashMap<>();
		private static final int sample_size_max = 10;
		private static final int sample_size_min = 10;
		
		public static LogSequenceStatistics execute(LogSequence logSequence) {
			DescriptiveStatistics sequenceNumExceptions;
			if(sequenceNumExceptionsMap.containsKey(logSequence.getSequenceName())){
				sequenceNumExceptions = sequenceNumExceptionsMap.get(logSequence.getSequenceName());
			} else {
				sequenceNumExceptions = new DescriptiveStatistics(sample_size_max);
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
				stats.addProperty("kurtosis", sequenceNumExceptions.getKurtosis());
				
				LogSequenceStatistics logSequenceStatistics = new LogSequenceStatistics(logSequence, "num_exceptions_statistics", stats);
				return logSequenceStatistics;
			}

			System.out.println(sequenceNumExceptions.getN());
			if(sequenceNumExceptions.getN() < sample_size_max){
				// Store the numExceptions of the current LogSequence
				sequenceNumExceptions.addValue(numExceptions);
				sequenceNumExceptionsMap.put(logSequence.getSequenceName(), sequenceNumExceptions);
			} else {
				System.out.println("Learning algorithm full: " + logSequence.getSequenceName());
			}
			return null;
		}

		private static int countExceptions(LogSequence logSequence){
			int numExceptions = 0;
			for (LogMessage logMessage : logSequence.getLogMessages()) {
				if(logMessage.getClassifications().containsKey("LOG_LEVEL") && logMessage.getClassifications().get("LOG_LEVEL").getValue().contentEquals("ERROR"))
					numExceptions++;
			}
			return numExceptions;
		}
		
	}
	
	private static class DurationStatisticsGatherer {
		private static Map<String, DescriptiveStatistics> sequenceDurationsMap = new HashMap<>();
		private static final int sample_size_max = 10;
		private static final int sample_size_min = 10;
	
		public static LogSequenceStatistics execute(LogSequence logSequence) {
			DescriptiveStatistics sequenceDurations;
			if(sequenceDurationsMap.containsKey(logSequence.getSequenceName())){
				sequenceDurations = sequenceDurationsMap.get(logSequence.getSequenceName());
			} else {
				sequenceDurations = new DescriptiveStatistics(sample_size_max);
			}
	
			Long duration = getLogSequenceDuration(logSequence);
			if(duration == null){
				System.out.println("Could not find duration of logsequence: " + logSequence);
				return null;
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
				stats.addProperty("kurtosis", sequenceDurations.getKurtosis());
				LogSequenceStatistics logSequenceStatistics = new LogSequenceStatistics(logSequence, "duration_statistics", stats);
				return logSequenceStatistics;
			}
	
			if(sequenceDurations.getN() < sample_size_max){
				// Store the numExceptions of the current LogSequence
				sequenceDurations.addValue(duration);
				sequenceDurationsMap.put(logSequence.getSequenceName(), sequenceDurations);
			} else {
				System.out.println("Learning algorithm full: " + logSequence.getSequenceName());
			}
			return null;
		}
	
		private static Long getLogSequenceDuration(LogSequence logSequence){
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

}
