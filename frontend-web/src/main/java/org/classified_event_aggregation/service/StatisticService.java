package org.classified_event_aggregation.service;

import java.util.List;

import org.classified_event_aggregation.model.Application;
import org.classified_event_aggregation.persistence.StatisticStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticService {

	@Autowired
	private StatisticStore statisticStore;
	
	public List<Application> getApplications(){
		return statisticStore.getApplications();
	}
}
