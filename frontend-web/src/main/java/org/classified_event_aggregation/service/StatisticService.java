package org.classified_event_aggregation.service;

import java.util.List;

import org.classified_event_aggregation.model.Application;
import org.classified_event_aggregation.persistence.LogSequenceStatisticsStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticService {

	@Autowired
	private LogSequenceStatisticsStore statisticStore;
	
	public List<Application> getApplications(){
		return statisticStore.getApplications();
	}
	
	public void getLogSequenceStatistics(String applicationName, String sequenceName, String algorithmName, int limit) {
		
	}

	public void getLogSequenceStatistics(String applicationName, String sequenceName, int limit) {
		
	}

	public void getLogSequenceStatistics(String applicationName, int limit) {

	}
}
