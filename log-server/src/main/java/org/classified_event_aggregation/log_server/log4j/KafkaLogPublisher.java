package org.classified_event_aggregation.log_server.log4j;

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.javaapi.producer.ProducerData;
import kafka.producer.ProducerConfig;

public class KafkaLogPublisher {

	private Producer<String, String> producer;

	public KafkaLogPublisher() {
		// @TODO make configurable
		Properties props = new Properties();
		props.put("zk.connect", "127.0.0.1:2181");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		ProducerConfig config = new ProducerConfig(props);
		producer = new Producer<String, String>(config);
	}

	public void send(ProducerData<String, String> data){
		producer.send(data);
	}

	public void close(){
		producer.close();
	}
}
