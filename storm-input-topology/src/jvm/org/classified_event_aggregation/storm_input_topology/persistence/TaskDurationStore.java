package org.classified_event_aggregation.storm_input_topology.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.trident.state.State;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

public class TaskDurationStore implements State {

	private Cluster cluster;
	private Session session;
	private Config config;

	private PreparedStatement insertDurationStmt;

	public static class Config {
		public String node;
	}

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(CassandraEventStore.class);

	/**
	 * @todo Make configurable via storm conf
	 */
	public TaskDurationStore() {
		this.config = new Config();
		cluster = Cluster.builder().addContactPoint(config.node).build();
		dropTablesAndKeySpace();
		createTablesAndKeySpace();
		session = cluster.connect("cea_duration");
		insertDurationStmt = session.prepare(
			"INSERT INTO task_duration_by_task_name (task_name, duration, start_time, end_time, task_id) VALUES (?, ?, ?, ?, ?)"
		);
	}

	public void createTablesAndKeySpace(){
		Session session = cluster.connect();

		session.execute(
			"CREATE KEYSPACE IF NOT EXISTS cea_duration " + 
				"WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 3};"
		);

		session.shutdown();
		session = cluster.connect("cea_duration");

		// Create event_counters table
		session.execute(
			"CREATE TABLE IF NOT EXISTS task_duration_by_task_name ( " + 
				"task_name text, " +
				"duration bigint, " +
				"start_time bigint, " +
				"end_time bigint, " +
				"task_id text, " +
				"PRIMARY KEY (task_name, end_time, task_id) " +
			") WITH CLUSTERING ORDER BY (end_time ASC);"
		);

		session.shutdown();
	}
	
	public void dropTablesAndKeySpace(){
		Session session = cluster.connect();
		session.execute("DROP KEYSPACE IF EXISTS cea_duration");
		session.shutdown();
	}


	public void storeDuration(String taskName, Long duration, Long startTime, Long endTime, String taskId) {
		session.execute(
				insertDurationStmt.bind(
				taskName,
				duration,
				startTime,
				endTime,
				taskId
			)
		);
	}


	@Override
	public void beginCommit(Long txid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void commit(Long txid) {
		// TODO Auto-generated method stub

	}

}
