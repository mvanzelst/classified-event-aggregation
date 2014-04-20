package org.classified_event_aggregation.storm_input_topology.persistence;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.classified_event_aggregation.storm_input_topology.model.LogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.trident.state.State;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class StatisticsStore implements State {

	private Cluster cluster;
	private Session session;
	private Config config;

	private PreparedStatement insertStmt;
	private PreparedStatement selectStmt;

	public static class Config {
		public String node;
		public String keySpace;
	}

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(StatisticsStore.class);

	/**
	 * @todo Make configurable via storm conf
	 */
	public StatisticsStore(Config config) {
		cluster = Cluster.builder().addContactPoint(config.node).build();
		dropTablesAndKeySpace();
		createTablesAndKeySpace();
		session = cluster.connect(config.keySpace);
		insertStmt = session.prepare("INSERT INTO log_sequence_statistic (sequenceId, sequenceName, descriptionHash, description, timestamp) VALUES (?, ?, ?, ?, ?)");
		selectStmt = session.prepare("SELECT * FROM log_message WHERE sequenceId = ? AND timestamp >= ? AND timestamp < ? ORDER BY timestamp ASC");
	}

	public void createTablesAndKeySpace(){
		Session session = cluster.connect();

		session.execute(
			"CREATE KEYSPACE IF NOT EXISTS cea " + 
					"WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 3};"
		);

		session.shutdown();
		session = cluster.connect("cea");

		session.execute(
			"CREATE TABLE IF NOT EXISTS log_sequence_statistic ( " +
				"applicationName text, " +
				"sequenceId text, " +
				"sequenceName text, " +
				"stats, " + 
				"timestamp bigint, " +
				"PRIMARY KEY (applicationName, sequenceName, timestamp) " +
			")"
		);

		session.shutdown();
	}

	public void dropTablesAndKeySpace(){
		Session session = cluster.connect();
		session.execute("DROP KEYSPACE IF EXISTS cea");
		session.shutdown();
	}

	@Override
	public void beginCommit(Long txid) {}

	@Override
	public void commit(Long txid) {}

	public void storeLogMessage(String description, Long timestamp, String sequenceName, String sequenceId){
		session.execute(
			insertStmt.bind(sequenceId, sequenceName, DigestUtils.md5Hex(description), description, timestamp)
		);
	}

	public List<LogMessage> retrieveLogMessages(Long startTimestamp, Long endTimestamp, String sequenceId){
		List<LogMessage> output = new ArrayList<>();

		ResultSet result = session.execute(
			selectStmt.bind(sequenceId, startTimestamp, endTimestamp)
		);

		for (Row row : result) {
			LogMessage logMessage = new LogMessage(row.getString("description"), row.getLong("timestamp"));
			output.add(logMessage);
		}
		return output;
	}

}