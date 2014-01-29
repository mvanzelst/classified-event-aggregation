package org.classified_event_aggregation.storm_input_topology.storm;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;
import backtype.storm.tuple.Values;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("serial")
public class ParseJSON extends BaseFunction {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private long counter = 0L;
	private long timer = System.currentTimeMillis();
	
	@Override
	public final void execute(
		final TridentTuple tuple,
		final TridentCollector collector
	) {
		statsHook();
		byte[] bytes = tuple.getBinary(0);
		String decoded = new String(bytes, Charset.forName("UTF-8"));
		if(log.isTraceEnabled()){
			log.trace("Received message: " + decoded);
		} else {
			log.debug("Received message");
		}
		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, String>>(){}.getType();
		Map<String, String> map;
		try {
			map = gson.fromJson(decoded, type);
		} catch (JsonSyntaxException e){
			log.error("Failed to parse json string: " + decoded, e);
			return;
		}
		collector.emit(new Values(
			map.get("description"),
			Long.parseLong(map.get("timestamp")),
			Arrays.asList(map.get("classifications").split(" "))
		));
	}
	
	public void statsHook(){
		counter++;
		if(counter % 500 == 0){
			System.out.println("processed " + counter + ", the last 500 took: " +  (System.currentTimeMillis() - timer) + "ms");
			timer = System.currentTimeMillis();
		}
	}
}
