package org.classified_event_aggregation.storm_input_topology.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.classified_event_aggregation.storm_input_topology.model.LogSequence;

import storm.trident.state.State;

public class NotificationStore implements State {

	public NotificationStore() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void beginCommit(Long txid){}

	public void commit(Long txid){}

	public void storeNotification(String description, double relevance, long timestamp, String algorithmName, LogSequence logSequence){
		try {
			Connection con = getConnection();
			PreparedStatement stmt = con.prepareStatement("INSERT INTO notification(description, relevance, timestamp, algorithm_name, logSequence, logSequenceId, logSequenceName) VALUES (?,?,?,?,?,?,?);");
			stmt.setString(1, description);
			stmt.setDouble(2, relevance);
			stmt.setLong(3, timestamp);
			stmt.setString(4, algorithmName);
			stmt.setString(5, logSequence.toJSON().toString());
			stmt.setString(6, logSequence.getSequenceId());
			stmt.setString(7, logSequence.getSequenceName());
			stmt.execute();
			con.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private Connection getConnection() throws SQLException{
		//@TODO make configurable
		return DriverManager.getConnection("jdbc:mysql://localhost/notification?user=notifier&password=notifier");
	}

}
