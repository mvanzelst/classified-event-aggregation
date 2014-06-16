package org.classified_event_aggregation.persistence;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

public class LogMessageStore {

	private Cluster cluster;
	private Session session;

	private PreparedStatement insertStmt;


	public static class Config {
		public String node;
		public String keySpace;
	}

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(LogMessageStore.class);

	/**
	 * @todo Make configurable via storm conf
	 */
	public LogMessageStore(Config config) {
		cluster = Cluster.builder().addContactPoint(config.node).build();
		createTables(config.keySpace);
		session = cluster.connect(config.keySpace);
		insertStmt = session.prepare("INSERT INTO log_message (sequenceId, sequenceName, descriptionHash, description, timestamp) VALUES (?, ?, ?, ?, ?)");
	}

	public void createTables(String keySpace){
		Session session = cluster.connect(keySpace);

		// Create event_counters table
		session.execute(
			"CREATE TABLE IF NOT EXISTS log_message ( " + 
				"sequenceId text, " +
				"sequenceName text, " +
				"descriptionHash text, " +
				"description text, " +
				"timestamp bigint, " +
				"PRIMARY KEY (sequenceId, timestamp, descriptionHash) " +
			")"
		);

		session.shutdown();
	}

	public void storeLogMessage(String description, Long timestamp, String sequenceName, String sequenceId){
		session.execute(
			insertStmt.bind(sequenceId, sequenceName, DigestUtils.md5Hex(description), description, timestamp)
		);
	}

}