package org.classified_event_aggregation.persistence;

import org.classified_event_aggregation.domain.LogSequenceStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

public class LogSequenceStatisticsStore {

	private Cluster cluster;
	private Session session;

	private PreparedStatement applicationInsertStmt;
	private PreparedStatement sequenceInsertStmt;
	private PreparedStatement algorithmInsertStmt;

	public static class Config {
		public String node;
		public String keySpace;
	}

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(LogSequenceStatisticsStore.class);

	/**
	 * @todo Make configurable via storm conf
	 */
	public LogSequenceStatisticsStore(Config config) {
		cluster = Cluster.builder().addContactPoint(config.node).build();
		createTables(config.keySpace);
		session = cluster.connect(config.keySpace);
		String insertTemplate = "INSERT INTO %s (applicationName, algorithmName, sequenceId, sequenceName, stats, endTimestamp, startTimestamp) VALUES (?,?,?,?,?,?,?)";
		applicationInsertStmt = session.prepare(String.format(insertTemplate, "log_sequence_statistics_by_application"));
		sequenceInsertStmt = session.prepare(String.format(insertTemplate, "log_sequence_statistics_by_sequence_name"));
		algorithmInsertStmt = session.prepare(String.format(insertTemplate, "log_sequence_statistics_by_algorithm_name"));		
	}

	public void createTables(String keySpace){
		Session session = cluster.connect(keySpace);

		session.execute(
			"CREATE TABLE IF NOT EXISTS log_sequence_statistics_by_application ( " +
				"applicationName text, " +
				"algorithmName text, " +
				"sequenceId text, " +
				"sequenceName text, " +
				"stats text, " + 
				"endTimestamp bigint, " +
				"startTimestamp bigint, " +
				"PRIMARY KEY (applicationName, endTimestamp, sequenceId, algorithmName) " +
			");"
		);
		
		session.execute(
			"CREATE TABLE IF NOT EXISTS log_sequence_statistics_by_sequence_name ( " +
				"applicationName text, " +
				"algorithmName text, " +
				"sequenceId text, " +
				"sequenceName text, " +
				"stats text, " + 
				"endTimestamp bigint, " +
				"startTimestamp bigint, " +
				"PRIMARY KEY ((applicationName, sequenceName), endTimestamp, sequenceId, algorithmName) " +
			");"
		);
		
		session.execute(
			"CREATE TABLE IF NOT EXISTS log_sequence_statistics_by_algorithm_name ( " +
				"applicationName text, " +
				"algorithmName text, " +
				"sequenceId text, " +
				"sequenceName text, " +
				"stats text, " + 
				"endTimestamp bigint, " +
				"startTimestamp bigint, " +
				"PRIMARY KEY ((applicationName, sequenceName, algorithmName), endTimestamp, sequenceId) " +
			");"
		);

		session.shutdown();
	}

	public void dropTablesAndKeySpace(String keySpace){
		Session session = cluster.connect();
		session.execute("DROP KEYSPACE IF EXISTS " + keySpace);
		session.shutdown();
	}

	public void storeLogSequenceStatistics(LogSequenceStatistics logSequenceStatistics){
		session.execute(
			applicationInsertStmt.bind(
				logSequenceStatistics.getApplicationName(),
				logSequenceStatistics.getAlgorithmName(),
				logSequenceStatistics.getSequenceId(),
				logSequenceStatistics.getSequenceName(),
				logSequenceStatistics.getStatistics().toString(),
				logSequenceStatistics.getEndTimestamp(),
				logSequenceStatistics.getStartTimestamp()
			)
		);
		session.execute(
			algorithmInsertStmt.bind(
				logSequenceStatistics.getApplicationName(),
				logSequenceStatistics.getAlgorithmName(),
				logSequenceStatistics.getSequenceId(),
				logSequenceStatistics.getSequenceName(),
				logSequenceStatistics.getStatistics().toString(),
				logSequenceStatistics.getEndTimestamp(),
				logSequenceStatistics.getStartTimestamp()
			)
		);
		session.execute(
			sequenceInsertStmt.bind(
				logSequenceStatistics.getApplicationName(),
				logSequenceStatistics.getAlgorithmName(),
				logSequenceStatistics.getSequenceId(),
				logSequenceStatistics.getSequenceName(),
				logSequenceStatistics.getStatistics().toString(),
				logSequenceStatistics.getEndTimestamp(),
				logSequenceStatistics.getStartTimestamp()
			)
		);
	}

}