package org.classified_event_aggregation.log_server.log4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import kafka.javaapi.producer.ProducerData;

import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * 
 * Based on {@link org.apache.log4j.net.SocketNode}
 * 
 * @author marijn
 * 
 */

public class SocketNode implements Runnable {
	private Socket socket;
	private ObjectInputStream ois;
	private KafkaLogPublisher kafkaLogPublisher;
	private final String APPLICATION_NAME;
	private final Layout DESCRIPTION_LAYOUT = new EnhancedPatternLayout("%d [%t] %-5p %-30.30c{1} - %m #SEQUENCE_NAME:%X{SEQUENCE_NAME} #SEQUENCE_ID:%X{SEQUENCE_ID} #LOG_LEVEL:%p");
	private final Layout DATE_LAYOUT = new EnhancedPatternLayout("%d{ISO8601}{UTC}");

	static Logger logger = Logger.getLogger(SocketNode.class);

	public SocketNode(Socket socket, String applicationName) {
		this.socket = socket;
		try {
			ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
		} catch (Exception e) {
			logger.error("Could not open ObjectInputStream to " + socket, e);
		}
		this.APPLICATION_NAME = applicationName;
	}

	public void run() {
		LoggingEvent event;
		Logger remoteLogger;
		
		try {
			kafkaLogPublisher = new KafkaLogPublisher();
			while (true) {
				// read an event from the wire
				event = (LoggingEvent) ois.readObject();
				String message = parseJSON(event).toString();
				System.out.println(message + "\n");
				kafkaLogPublisher.send(new ProducerData<String, String>("logs", message));
			}
		} catch (java.io.EOFException e) {
			logger.info("Caught java.io.EOFException closing conneciton.");
		} catch (java.net.SocketException e) {
			logger.info("Caught java.net.SocketException closing conneciton.");
		} catch (IOException e) {
			logger.info("Caught java.io.IOException: " + e);
			logger.info("Closing connection.");
		} catch (Exception e) {
			logger.error("Unexpected exception. Closing connection.", e);
		} finally {
			kafkaLogPublisher.close();
		}

		try {
			ois.close();
		} catch (Exception e) {
			logger.info("Could not close connection.", e);
		}
	}
	
	private JsonObject parseJSON(LoggingEvent event){
		JsonObject job = new JsonObject();
		job.addProperty("application_name", APPLICATION_NAME);
		job.addProperty("description", DESCRIPTION_LAYOUT.format(event));
		job.addProperty("date", DATE_LAYOUT.format(event));
		return job;
	}
}
