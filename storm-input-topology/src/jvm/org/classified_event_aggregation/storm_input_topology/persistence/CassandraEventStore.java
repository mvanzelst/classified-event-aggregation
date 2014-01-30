package org.classified_event_aggregation.storm_input_topology.persistence;

import java.util.Collection;

import org.classified_event_aggregation.storm_input_topology.model.Classification;
import org.classified_event_aggregation.storm_input_topology.model.ClassifiedEvent;
import org.classified_event_aggregation.storm_input_topology.model.time.TimePeriod;

import scala.actors.threadpool.Arrays;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class CassandraEventStore implements EventStore {

	private Cluster cluster;
	private Session session;
	private Config config;

	private PreparedStatement insertTimelineStmt;
	private PreparedStatement setEventCounterByClassificationKeyStmt;
	private PreparedStatement setEventCounterByClassificationStmt;
	private PreparedStatement getEventCounterByClassificationStmt;
	private Long txid;
	
	public static class Config {
		public String node;
	}

	/**
	 * @todo Make configurable via storm conf
	 */
	public CassandraEventStore() {
		this.config = new Config();
		cluster = Cluster.builder().addContactPoint(config.node).build();
		dropTablesAndKeySpace();
		createTablesAndKeySpace();
		session = cluster.connect("cea");
		insertTimelineStmt = session.prepare("INSERT INTO event_timeline (timestamp, classification, description) VALUES (?, ?, ?)");
		setEventCounterByClassificationKeyStmt = session.prepare(
			"UPDATE event_counters_by_classification_key SET " +
				"counter = ?, " +
				"txid = ?, " +
				"last_description = ? " +
			"WHERE " + 
				"period_type_name = ? AND " +
				"period_start = ? AND " +
				"classification_key = ? AND " +
				"classification_value = ?;"
		);
		setEventCounterByClassificationStmt = session.prepare(
			"UPDATE event_counters_by_classification SET " +
					"counter = ?, " +
					"txid = ?, " +
					"last_description = ? " +
			"WHERE " + 
				"period_type_name = ? AND " +
				"period_start = ? AND " +
				"classification = ?;"
		);
		getEventCounterByClassificationStmt = session.prepare(
			"SELECT counter " +
			"FROM event_counters_by_classification " +
			"WHERE " + 
				"period_type_name = ? AND " +
				"period_start = ? AND " +
				"classification = ?"
		);
	}

	public void createTablesAndKeySpace(){
		Session session = cluster.connect();

		session.execute(
			"CREATE KEYSPACE IF NOT EXISTS cea " + 
					"WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 3};"
		);

		session.shutdown();
		session = cluster.connect("cea");

		// Create event_counters table
		session.execute(
			"CREATE TABLE IF NOT EXISTS event_counters_by_classification_key ( " + 
				"period_type_name text, " +
				"period_start bigint, " +
				"classification_key text, " +
				"classification_value text, " +
				"counter bigint, " +
				"txid bigint, " +
				"last_description text, " +
				"PRIMARY KEY ((classification_key, period_type_name), period_start, classification_value) " +
			") WITH CLUSTERING ORDER BY (period_start ASC, classification_value ASC);"
		);

		session.execute(
			"CREATE TABLE IF NOT EXISTS event_counters_by_classification ( " + 
				"period_type_name text, " +
				"period_start bigint, " +
				"classification text, " +
				"counter bigint, " +
				"txid bigint, " +
				"last_description text, " +
				"PRIMARY KEY ((classification, period_type_name), period_start) " +
			") WITH CLUSTERING ORDER BY (period_start ASC);"
		);

		// Create event_timeline table
		session.execute(
			"CREATE TABLE IF NOT EXISTS event_timeline ( " + 
				"timestamp bigint, " +
				"classification text, " +
				"description text, " +
				"PRIMARY KEY ((classification), timestamp) " +
			") WITH CLUSTERING ORDER BY (timestamp ASC);"
		);
		session.shutdown();
	}
	
	public void dropTablesAndKeySpace(){
		Session session = cluster.connect();
		session.execute("DROP KEYSPACE IF EXISTS cea");
		session.shutdown();
	}

	@Override
	public void beginCommit(Long txid) {
		this.txid = txid;
	}

	@Override
	public void commit(Long txid) {
		this.txid = null;
	}

	@Override
	public void setClassificationCounter(String periodTypeName, Long periodStart, String lastDescription, Classification classification, Long amount) {
		session.execute(
			setEventCounterByClassificationKeyStmt.bind(
				amount,
				this.txid,
				lastDescription,
				periodTypeName,
				periodStart,
				classification.getKey(),
				classification.getValue()
			)
		);
		session.execute(
			setEventCounterByClassificationStmt.bind(
				amount,
				this.txid,
				lastDescription,
				periodTypeName,
				periodStart,
				classification.toString()
			)
		);
	}

	@Override
	public void storeClassifiedEvent(ClassifiedEvent event) {
		Collection<ClassifiedEvent> derivedEvents = event.getDerivedEvents();
		for (ClassifiedEvent classifiedEvent : derivedEvents) {
			session.execute(
				insertTimelineStmt.bind(
					classifiedEvent.getEvent().getTimestamp(), 
					classifiedEvent.getClassifications().iterator().next().toString(),
					classifiedEvent.getEvent().getDescription()
				)
			);
		}
	}

	@Override
	public Long getClassificationCounter(String periodTypeName, Long periodStart, Classification classification) {
		ResultSet result = session.execute(
			getEventCounterByClassificationStmt.bind(
				periodTypeName,
				periodStart,
				classification.toString()
			)
		);
		Row row = result.one();
		if(result.one() == null){
			return 0L;
		}
		return row.getLong(0);
	}

}
