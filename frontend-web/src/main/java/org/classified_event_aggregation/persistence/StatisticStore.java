package org.classified_event_aggregation.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.classified_event_aggregation.model.Application;
import org.classified_event_aggregation.model.Task;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

@Repository
public class StatisticStore {

	private Session session;

	@PostConstruct
	private void setCassandraConnection(){
		Cluster cluster = Cluster.builder().addContactPoint("localhost").build();
		this.session = cluster.connect("cea");
	}
	
	@PreDestroy
	private void closeCassandraConnection(){
		this.session.shutdown();
	}
	
	

	public List<Application> getApplications(){
		return Arrays.asList(
			new Application("Xtranet Prod", getTasks("Xtranet Prod")), 
			new Application("Xtranet Acc", getTasks("Xtranet Acc"))
		);
	}

	public List<Task> getTasks(String applicationName){
		if(applicationName.equals("Xtranet Prod")){
			return Arrays.asList(new Task("prod task 1"), new Task("prod task 2"));
		} else if(applicationName.equals("Xtranet Acc")){
			return Arrays.asList(new Task("acc task 1"), new Task("acc task 2"));
		} else {
			return new ArrayList<>();
		}
	}
	/*
	 * GenericConversionService service = new DefaultConversionService();
		Long start = service.convert(startParam, Long.class);
		Long end = service.convert(endParam, Long.class);
		Boolean reverse = service.convert(reverseParam, Boolean.class);
		Integer limit = service.convert(limitParam, Integer.class);

		Session session = getCassandraConnection();
		Select query = QueryBuilder
			.select("classification_value", "period_start", "counter")
			.from("event_counters_by_classification_key")
			.where(QueryBuilder.eq("period_type_name", periodTypeName))
				.and(QueryBuilder.eq("classification_key", classificationKey))
			.orderBy((!reverse ? QueryBuilder.asc("period_start") : QueryBuilder.desc("period_start")));

		if(start != null)
			query.where(QueryBuilder.gt("period_start", start));

		if(end != null)
			query.where(QueryBuilder.lte("period_start", end));

		query.limit(limit);

		ResultSet result = session.execute(query);
		JsonArray arr = new JsonArray();
		for (Row row : result) {
			JsonObject jobject = new JsonObject();
			jobject.add("period_start", new JsonPrimitive(row.getLong("period_start")));
			jobject.add("counter", new JsonPrimitive(row.getLong("counter")));
			jobject.add("classification_value", new JsonPrimitive(row.getString("classification_value")));
			arr.add(jobject);
		}
		return arr.toString();
	 */
	
}
