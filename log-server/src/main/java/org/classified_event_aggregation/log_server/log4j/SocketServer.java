package org.classified_event_aggregation.log_server.log4j;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * Based on {@link org.apache.log4j.net.SocketServer}
 * 
 * @author marijn
 * 
 */
public class SocketServer {
	static String GENERIC = "generic";
	static String CONFIG_FILE_EXT = ".lcf";

	static Logger logger = Logger.getLogger(SocketServer.class);
	static int port;

	public static void main(String argv[]) {
		if (argv.length == 1)
			init(argv[0]);
		else
			usage("Wrong number of arguments.");

		try {
			logger.info("Listening on port " + port);
			ServerSocket serverSocket = new ServerSocket(port);
			while (true) {
				logger.info("Waiting to accept a new client.");
				Socket socket = serverSocket.accept();
				InetAddress inetAddress = socket.getInetAddress();
				logger.info("Connected to client at " + inetAddress);
				logger.info("Starting new socket node.");
				new Thread(new SocketNode(socket)).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void usage(String msg) {
		System.err.println(msg);
		System.err.println("Usage: java " + SocketServer.class.getName() + " port");
		System.exit(1);
	}

	static void init(String portStr) {
		try {
			port = Integer.parseInt(portStr);
		} catch (java.lang.NumberFormatException e) {
			e.printStackTrace();
			usage("Could not interpret port number [" + portStr + "].");
		}
	}
}
