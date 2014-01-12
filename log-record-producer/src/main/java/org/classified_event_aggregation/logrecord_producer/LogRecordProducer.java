package org.classified_event_aggregation.logrecord_producer;

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.javaapi.producer.ProducerData;
import kafka.producer.ProducerConfig;

public class LogRecordProducer {

	public static void main(String args[]) {
		Properties props = new Properties();
		props.put("zk.connect", "127.0.0.1:2181");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		ProducerConfig config = new ProducerConfig(props);
		Producer<String, String> producer = new Producer<String, String>(config);
		// Send a single message
		// The message is sent to a randomly selected partition registered in ZK
		ProducerData<String, String> data = new ProducerData<String, String>("test", "test-message");
		producer.send(data);
	}
}
