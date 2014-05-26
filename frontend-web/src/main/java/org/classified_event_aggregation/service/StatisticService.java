package org.classified_event_aggregation.service;

import java.util.ArrayList;
import java.util.List;

import org.classified_event_aggregation.model.Application;
import org.classified_event_aggregation.model.LogSequenceStatistics;
import org.classified_event_aggregation.persistence.LogSequenceStatisticsStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@Service
public class StatisticService {

	@Autowired
	private LogSequenceStatisticsStore statisticStore;

	public List<Application> getApplications(){
		return statisticStore.getApplications();
	}

	public List<LogSequenceStatistics> getLogSequenceStatistics(String applicationName, int limit, Long start, Long end, boolean reverse) {
		return statisticStore.getLogSequenceStatistics(applicationName, start, end, limit, reverse);
	}

	public List<LogSequenceStatistics> getLogSequenceStatistics(String applicationName, String sequenceName, int limit, Long start, Long end, boolean reverse) {
		return statisticStore.getLogSequenceStatistics(applicationName, sequenceName, start, end, limit, reverse);
	}

	public List<LogSequenceStatistics> getLogSequenceStatistics(String applicationName, String sequenceName, String algorithmName, int limit, Long start, Long end, boolean reverse) {
		return statisticStore.getLogSequenceStatistics(applicationName, sequenceName, algorithmName, start, end, limit, reverse);
	}
	
	public List<String> getDistinctAlgorithmNames(String applicationName, String sequenceName){
		return statisticStore.getAlgorithmNames(applicationName, sequenceName);
	}
	
	public JsonArray getStandardScoresOfDuration(String applicationName, String sequenceName, int limit, Long start, Long end, boolean reverse){
		return getStandardScoresOfStatisticsField(applicationName, sequenceName, "duration", "duration_statistics", limit, start, end, reverse);
	}
	
	public JsonArray getDurations(String applicationName, String sequenceName, int limit, Long start, Long end, boolean reverse){
		return getRawValueOfStatisticsField(applicationName, sequenceName, "duration", "duration_statistics", limit, start, end, reverse);
	}
	
	public JsonArray getStandardScoresOfNumExceptions(String applicationName, String sequenceName, int limit, Long start, Long end, boolean reverse){
		return getStandardScoresOfStatisticsField(applicationName, sequenceName, "num_exceptions", "num_exceptions_statistics", limit, start, end, reverse);
	}
	
	public JsonArray getNumExceptions(String applicationName, String sequenceName, int limit, Long start, Long end, boolean reverse){
		return getRawValueOfStatisticsField(applicationName, sequenceName, "num_exceptions", "num_exceptions_statistics", limit, start, end, reverse);
	}
	
	private JsonArray getRawValueOfStatisticsField(String applicationName, String sequenceName, String fieldName, String algorithmName, int limit, Long start, Long end, boolean reverse){
		JsonArray output = new JsonArray();
		List<LogSequenceStatistics> logSequenceStatistics = getLogSequenceStatistics(applicationName, sequenceName, algorithmName, limit, start, end, reverse);
		for (LogSequenceStatistics logSequenceStatisticsObject : logSequenceStatistics) {
			JsonObject statistics = logSequenceStatisticsObject.getStatistics();
			output.add(statistics.get(fieldName));
		}
		return output;
	}
	
	private JsonArray getStandardScoresOfStatisticsField(String applicationName, String sequenceName, String fieldName, String algorithmName, int limit, Long start, Long end, boolean reverse){
		JsonArray output = new JsonArray();
		List<LogSequenceStatistics> logSequenceStatistics = getLogSequenceStatistics(applicationName, sequenceName, algorithmName, limit, start, end, reverse);
		for (LogSequenceStatistics logSequenceStatisticsObject : logSequenceStatistics) {
			JsonObject statistics = logSequenceStatisticsObject.getStatistics();
			double stdDev = statistics.get("standard_deviation").getAsDouble();
			if(stdDev == 0){
				output.add(new JsonPrimitive(0));
				continue;
			}
			double mean = statistics.get("mean").getAsDouble();
			double observation = statistics.get(fieldName).getAsDouble();
			double stdScore = (observation - mean) / stdDev;
			output.add(new JsonPrimitive(stdScore));
		}
		return output;
	}
}
