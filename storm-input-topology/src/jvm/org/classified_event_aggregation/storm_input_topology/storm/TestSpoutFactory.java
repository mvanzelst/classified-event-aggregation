package org.classified_event_aggregation.storm_input_topology.storm;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;

import storm.trident.operation.TridentCollector;
import storm.trident.testing.FixedBatchSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class TestSpoutFactory {

	private static Long time = System.currentTimeMillis();
	private static int timeCount = 0;
	
	@SuppressWarnings("serial")
	public static class CustomFixedBatchSpout extends FixedBatchSpout {
		private int sleepTime;
		
		public CustomFixedBatchSpout(Fields fields, int maxBatchSize,
				List<Object>[] outputs, int sleepTime) {
			super(fields, maxBatchSize, outputs);
			this.sleepTime = sleepTime;
		}
		
		@Override
		public void emitBatch(long batchId, TridentCollector collector) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			super.emitBatch(batchId, collector);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public static FixedBatchSpout build() {
		
		FixedBatchSpout spout = new CustomFixedBatchSpout(
			new Fields("bytes"), 
			1000, 
			getValues(500000),
			1
		);
		spout.setCycle(false);
		return spout;
	}
	
	private static List<Object>[] getValues(int amount){
		List<Object>[] output = (List<Object>[]) new ArrayList<?>[amount];
		for (int i = 0; i < amount; i++) {
			output[i] = getValue();
		}
		return output;
	}
	
	@SuppressWarnings("unchecked")
	private static List<Object> getValue(){
		return new Values(
			("{description: '" + RandomStringUtils.randomAlphanumeric(100) + "', timestamp: " + getIncrementingTimestamp() + ", classifications: '" + getRandomClassifications(1) + "'}")
				.replace("'", "\"")
				.getBytes(Charset.forName("UTF-8"))
		);
	}
	
	private static Long getIncrementingTimestamp(){
		// Give every timestamp 10 times
		if(++timeCount > 10){
			time += 3600000L;
			timeCount=0;
		}
		return time;
	}
	
	
	private static String getRandomClassifications(int number){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < number; i++) {
			 String[] classifications = new String[]{
				"LogLevel:Error",
				"LogLevel:Warn",
				"LogLevel:Debug"
			};

			sb.append(randomFromArray(classifications));
			if(i+1<number){ // Not the last iteration
				sb.append(" ");
			}
		}
		return sb.toString();
	}
	
	private static String randomFromArray(String[] array){
		return array[new Random().nextInt(array.length)];
	}
}
