package org.classified_event_aggregation.log_server.log4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * 
 * Based on {@link org.apache.log4j.net.SocketNode}
 * 
 * @author marijn
 * 
 */

public class SocketNode implements Runnable {
	Socket socket;
	ObjectInputStream ois;

	static Logger logger = Logger.getLogger(SocketNode.class);

	public SocketNode(Socket socket) {
		this.socket = socket;
		try {
			ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
		} catch (Exception e) {
			logger.error("Could not open ObjectInputStream to " + socket, e);
		}
	}

	public void run() {
		LoggingEvent event;
		Logger remoteLogger;
		Layout patternLayout = new EnhancedPatternLayout("{\"description\": \"%d [%t] %-5p %-30.30c{1} - %m #TASK_NAME:%X{TASK_NAME} #TASK_ID:%X{TASK_ID} #LOG_LEVEL:%p\", \"date\": \"%d{ISO8601}{UTC}\"} %n");
		try {
			while (true) {
				// read an event from the wire
				event = (LoggingEvent) ois.readObject();

				// @TODO add kafka connection here
				System.out.println(patternLayout.format(event));
			}
		} catch (java.io.EOFException e) {
			logger.info("Caught java.io.EOFException closing conneciton.");
		} catch (java.net.SocketException e) {
			logger.info("Caught java.net.SocketException closing conneciton.");
		} catch (IOException e) {
			logger.info("Caught java.io.IOException: " + e);
			logger.info("Closing connection.");
		} catch (Exception e) {
			logger.error("Unexpected exception. Closing conneciton.", e);
		}

		try {
			ois.close();
		} catch (Exception e) {
			logger.info("Could not close connection.", e);
		}
	}
}
