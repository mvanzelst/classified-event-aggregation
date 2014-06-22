package org.classified_event_aggregation.service;

import java.util.List;

import org.classified_event_aggregation.domain.Application;
import org.classified_event_aggregation.domain.DimensionlessStatisticType;
import org.classified_event_aggregation.domain.LogSequenceStatistics;
import org.classified_event_aggregation.persistence.cassandra.LogSequenceStatisticsStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

@Service
public class StatisticService {

	public final static int MAX_VALUE = 65535;
	
	@Autowired
	private LogSequenceStatisticsStore statisticStore;

	public List<Application> getApplications(){
		return statisticStore.getApplications();
	}
	
	public List<LogSequenceStatistics> getDerivedStatistic(String applicationName, String sequenceName, int limit, Long start, Long end, boolean reverse, DimensionlessStatisticType statisticType){
		String fieldName;
		final String algorithmName;
		switch (statisticType) {
			case STANDARD_SCORE_OF_NUMBER_OF_EXCEPTIONS:
					fieldName = "num_exceptions";
					algorithmName = "num_exceptions_statistics";
				break;
			case STANDARD_SCORE_OF_SEQUENCE_DURATION:
					fieldName = "duration";
					algorithmName = "duration_statistics";
				break;
			default:
				throw new IllegalArgumentException();
		}

		List<LogSequenceStatistics> logSequenceStatistics = statisticStore.getLogSequenceStatistics(applicationName, sequenceName, "", start, end, limit * 2, reverse);

		logSequenceStatistics = Lists.newArrayList(Iterables.filter(logSequenceStatistics, new Predicate<LogSequenceStatistics>() {
			@Override
			public boolean apply(LogSequenceStatistics input) {
				return input.getAlgorithmName().contentEquals(algorithmName);
			}
		}));

		// Augment statistics with derived statistics
		for (LogSequenceStatistics logSequenceStatisticsObject : logSequenceStatistics) {
			JsonObject statistics = logSequenceStatisticsObject.getStatistics();
			double stdDev = statistics.get("standard_deviation").getAsDouble();
			double mean = statistics.get("mean").getAsDouble();
			double observation = statistics.get(fieldName).getAsDouble();
			if(stdDev == 0){
				if((observation - mean) > 0){
					statistics.addProperty("stdScore", MAX_VALUE);
				} else {
					statistics.addProperty("stdScore", 0);
				}
				continue;
			}
			double stdScore = (observation - mean) / stdDev;
			statistics.addProperty("stdScore", stdScore);
		}
		return logSequenceStatistics;
	}
}
