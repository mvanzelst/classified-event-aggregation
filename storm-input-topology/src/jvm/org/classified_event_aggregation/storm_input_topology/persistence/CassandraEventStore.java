package org.classified_event_aggregation.storm_input_topology.persistence;

import java.util.Collection;

import org.classified_event_aggregation.storm_input_topology.model.Classification;
import org.classified_event_aggregation.storm_input_topology.model.ClassifiedEvent;
import org.classified_event_aggregation.storm_input_topology.model.time.TimePeriod;

import scala.actors.threadpool.Arrays;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

public class CassandraEventStore implements EventStore {

	private Cluster cluster;
	private Session session;
	private Config config;
	private Long txid;

	public static class Config {
		public String node;

		@SuppressWarnings("unchecked")
		public Collection<TimePeriod> timePeriods = 
			Arrays.asList(new TimePeriod[]{
				new TimePeriod("year", "y"), // 2014
				new TimePeriod("month", "y'-M'MM"), // 2014-M01
				new TimePeriod("week", "x'-W'ww"), // 2014-W04
				new TimePeriod("date", "y-MM-dd"), // 2014-01-25
				new TimePeriod("hour", "y-MM-dd 'H-'HH"), // 2014-01-25 H01
				new TimePeriod("minute", "y-MM-dd HH:mm") // 2014-01-25 01:15
			});
	}

	public CassandraEventStore(Config conf) {
		this.config = conf;
		cluster = Cluster.builder().addContactPoint(conf.node).build();
	}

	@Override
	public void beginCommit(Long txid) {
		session = cluster.connect();
		this.txid = txid;
	}

	@Override
	public void commit(Long txid) {
		session.shutdown();
		this.txid = null;
	}


	@Override
	public void incrementClassificationCounter(Classification classification, Long timestamp) {
		for (TimePeriod timePeriod : config.timePeriods) {
			PreparedStatement stmt = session.prepare(
				"UPDATE event_counters SET "
					+ "period_name = ?, "
					+ "period_value = ?,  "
					+ "classification = ?, "
					+ "counter = counter + ?, "
					+ "txid = ?"
			);
			session.execute(
				stmt.bind(
					timePeriod.getName(),
					timePeriod.convertTimestampToDateFormat(timestamp),
					classification.toString(),
					1,
					txid
				)
			);
		}
	}

	@Override
	public void storeClassifiedEvent(ClassifiedEvent event) {
		Collection<ClassifiedEvent> derivedEvents = event.getDerivedEvents();
		for (ClassifiedEvent classifiedEvent : derivedEvents) {
			PreparedStatement stmt = session.prepare("INSERT INTO event_timeline (timestamp, classification, description) VALUES (?, ?, ?)");
			session.execute(
				stmt.bind(
					classifiedEvent.getEvent().getTimestamp(), 
					classifiedEvent.getClassifications().iterator().next(),
					classifiedEvent.getEvent().getDescription()
				)
			);
		}
	}

}
