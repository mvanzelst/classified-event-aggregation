package org.classified_event_aggregation.persistence.cassandra;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Repository;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

@Repository
public class LogMessageStore {

	private Session session;

	@PostConstruct
	private void setCassandraConnection(){
		Cluster cluster = Cluster.builder().addContactPoint("localhost").build();
		this.session = cluster.connect("cea_demo");
	}
	
	@PreDestroy
	private void closeCassandraConnection(){
		this.session.shutdown();
	}

	public List<String> getLogMessages(String sequenceId){
		Statement stmt = QueryBuilder
			.select()
			.column("description")
			.from("log_message")
			.where(QueryBuilder.eq("sequenceid", sequenceId))
			.orderBy(QueryBuilder.asc("timestamp"));

		ResultSet resultSet = session.execute(stmt);

		List<String> logMessages = new ArrayList<>();
		for (Row row : resultSet)
			logMessages.add(row.getString("description"));
		
		return logMessages; 
	}

}
