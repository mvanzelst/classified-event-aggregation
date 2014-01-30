package org.classified_event_aggregation.storm_input_topology.storm;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;

import storm.trident.testing.FixedBatchSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class TestSpoutFactory {

	private static Long time = System.currentTimeMillis();
	
	@SuppressWarnings("serial")
	public static class TestFixedBatchSpout extends FixedBatchSpout {

		public TestFixedBatchSpout(Fields fields, int maxBatchSize, List<Object>[] outputs) {
			super(fields, maxBatchSize, outputs);
			// TODO Auto-generated constructor stub
		}
		
	}
	@SuppressWarnings("unchecked")
	public static FixedBatchSpout build() {
		FixedBatchSpout spout = new FixedBatchSpout(
			new Fields("bytes"), 
			1000, 
			getValues(500000)
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
			("{description: '" + RandomStringUtils.randomAlphanumeric(100) + "', timestamp: " + getIncrementingTimestamp() + ", classifications: '" + getRandomClassifications(3) + "'}")
				.replace("'", "\"")
				.getBytes(Charset.forName("UTF-8"))
		);
	}
	
	private static Long getIncrementingTimestamp(){
		time += 10000000L;
		return time;
	}
	
	
	private static String getRandomClassifications(int number){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < number; i++) {
			 String[] classifications = new String[]{
				"LogLevel:Error",
				"LogLevel:Warn",
				"LogLevel:Debug",
				"User:1",
				"User:2"
			};

			sb.append(randomFromArray(classifications));
			if(i+1<number){ // No the last iteration
				sb.append(" ");
			}
		}
		return sb.toString();
	}
	
	private static String randomFromArray(String[] array){
		return array[new Random().nextInt(array.length)];
	}
}
