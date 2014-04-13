package org.classified_event_aggregation.storm_input_topology.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.classified_event_aggregation.storm_input_topology.model.LogSequence;
import org.classified_event_aggregation.storm_input_topology.model.Notification;

import storm.trident.state.State;

import com.google.gson.JsonParser;

public class NotificationStore implements State {

	private final Config config;
	public static class Config {
		public String user;
		public String password;
		public String host;
		public String db;
	}

	public NotificationStore(Config config) {
		this.config = config;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void beginCommit(Long txid){}

	public void commit(Long txid){}

	public void storeNotification(Notification notification){
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = getConnection();
			stmt = con.prepareStatement("INSERT INTO notification(description, relevance, timestamp, algorithm_name, log_sequence, log_sequence_id, log_sequence_name) VALUES (?,?,?,?,?,?,?);");
			stmt.setString(1, notification.getDescription());
			stmt.setDouble(2, notification.getRelevance());
			stmt.setLong(3, notification.getTimestamp());
			stmt.setString(4, notification.getAlgorithmName());
			stmt.setString(5, notification.getLogSequence().toJSON().toString());
			stmt.setString(6, notification.getLogSequence().getSequenceId());
			stmt.setString(7, notification.getLogSequence().getSequenceName());
			stmt.execute();
		} catch (SQLException e) {
			throw new RuntimeException("Data access exception", e);
		} finally { // Ah the joys of try catch finally try catch
			try {
				if(stmt != null)
					stmt.close();
			} catch (Exception e) {}

			try {
				if(con != null)
					con.close();
			} catch (Exception e) {}
		}
	}

	private Connection getConnection() throws SQLException {
		String connectionString = String.format("jdbc:mysql://%s/%s?user=%s&password=%s", config.host, config.db, config.user, config.password);
		return DriverManager.getConnection(connectionString);
	}
	
	public Collection<Notification> getAllNotifications(){
		Collection<Notification> output = new ArrayList<>();
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			con = getConnection();
			stmt = con.prepareStatement("SELECT * FROM notification");
			stmt.execute();
			resultSet = stmt.getResultSet();
			while(resultSet.next()){
				Notification notification = new Notification();
				notification.setAlgorithmName(resultSet.getString("algorithm_name"));
				notification.setDescription(resultSet.getString("description"));
				notification.setLogSequence(
					LogSequence.fromJSON(
						new JsonParser().parse(resultSet.getString("log_sequence")).getAsJsonObject()
					)
				);
				notification.setRelevance(resultSet.getDouble("relevance"));
				notification.setTimestamp(resultSet.getLong("timestamp"));
				output.add(notification);
			}
			return output;
		} catch (SQLException e) {
			throw new RuntimeException("Data access exception", e);
		} finally { // Ah the joys of try catch finally try catch
			try {
				if(resultSet != null)
					resultSet.close();
			} catch (Exception e) {}
			
			try {
				if(stmt != null)
					stmt.close();
			} catch (Exception e) {}

			try {
				if(con != null)
					con.close();
			} catch (Exception e) {}
		}
	}
	
	public void purgeNotifications(){
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = getConnection();
			stmt = con.prepareStatement("TRUNCATE TABLE notification");
			stmt.execute();
		} catch (SQLException e) {
			throw new RuntimeException("Data access exception", e);
		} finally { // Ah the joys of try catch finally try catch
			try {
				if(stmt != null)
					stmt.close();
			} catch (Exception e) {}

			try {
				if(con != null)
					con.close();
			} catch (Exception e) {}
		}
	}

}
