package org.classified_event_aggregation.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.classified_event_aggregation.model.Application;
import org.classified_event_aggregation.model.LogSequence;
import org.classified_event_aggregation.model.LogSequenceStatistics;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.gson.JsonParser;

@Repository
public class LogSequenceStatisticsStore {

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

	public List<String> getAlgorithmNames(String applicationName, String sequenceName){
		Statement stmt = QueryBuilder
			.select()
			.distinct()
			.column("applicationName")
			.column("sequenceName")
			.column("algorithmName")
			.from("log_sequence_statistics_by_algorithm_name");

		ResultSet resultSet = session.execute(stmt);

		List<String> algorithmNames = new ArrayList<>();
		for (Row row : resultSet)
			if(row.getString("applicationName").contentEquals(applicationName) && row.getString("sequenceName").contentEquals(sequenceName))
				algorithmNames.add(row.getString("algorithmName"));

		return algorithmNames;
	}
	
	public List<Application> getApplications(){
		ResultSet resultSet = this.session.execute("SELECT DISTINCT applicationName, sequenceName FROM log_sequence_statistics_by_sequence_name");

		Map<String, Application> applications = new HashMap<>();
		for (Row row : resultSet) {
			String applicationName = row.getString("applicationName");
			String sequenceName = row.getString("sequenceName");
			if(applications.containsKey(applicationName)){
				List<LogSequence> sequences = applications.get(applicationName).getSequences();
				sequences.add(new LogSequence(sequenceName));
			} else {
				List<LogSequence> sequences = new ArrayList<LogSequence>();
				sequences.add(new LogSequence(sequenceName));
				applications.put(applicationName, new Application(applicationName, sequences));
			}
		}
		return new ArrayList<>(applications.values());
	}
	
	public List<LogSequenceStatistics> getLogSequenceStatistics(String applicationName, Long start, Long end, int limit, boolean reverse){
		return getLogSequenceStatistics(applicationName, null, null, start, end, limit, reverse, "log_sequence_statistics_by_application_name");
	}
	
	public List<LogSequenceStatistics> getLogSequenceStatistics(String applicationName, String sequenceName, Long start, Long end, int limit, boolean reverse){
		return getLogSequenceStatistics(applicationName, sequenceName, null, start, end, limit, reverse, "log_sequence_statistics_by_sequence_name");
	}
	
	public List<LogSequenceStatistics> getLogSequenceStatistics(String applicationName, String sequenceName, String algorithmName, Long start, Long end, int limit, boolean reverse){
		return getLogSequenceStatistics(applicationName, sequenceName, algorithmName, start, end, limit, reverse, "log_sequence_statistics_by_algorithm_name");
	}

	private List<LogSequenceStatistics> getLogSequenceStatistics(String applicationName, String sequenceName, String algorithmName, Long start, Long end, int limit, boolean reverse, String tableName){
		Select query = QueryBuilder
				.select().all()
				.from(tableName);

		query.where(QueryBuilder.eq("applicationName", applicationName));

		if(sequenceName != null)
			query.where(QueryBuilder.eq("sequenceName", sequenceName));

		if(algorithmName != null)
			query.where(QueryBuilder.eq("algorithmName", algorithmName));

		if(start != null)
			query.where(QueryBuilder.gte("endTimestamp", start));

		if(end != null)
			query.where(QueryBuilder.lt("endTimestamp", end));

		query.orderBy((!reverse ? QueryBuilder.asc("endTimestamp") : QueryBuilder.desc("endTimestamp")));

		List<LogSequenceStatistics> output = new ArrayList<>();
		ResultSet result = session.execute(query);
		JsonParser jsonParser = new JsonParser();
		for (Row row : result) {
			LogSequenceStatistics logSequenceStatistics = new LogSequenceStatistics (
				row.getString("applicationName"),
				row.getString("algorithmName"),
				row.getString("sequenceName"),
				row.getString("sequenceId"),
				row.getLong("startTimestamp"),
				row.getLong("endTimestamp"),
				jsonParser.parse(row.getString("stats")).getAsJsonObject()
			);
			output.add(logSequenceStatistics);
		}
		return output;
	}

}
