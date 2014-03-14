package org.classified_event_aggregation.storm_input_topology.storm;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.classified_event_aggregation.storm_input_topology.model.LogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.TridentOperationContext;
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
	private SimpleDateFormat parserSDF;

	@Override
	public void prepare(Map conf, TridentOperationContext context) {
		super.prepare(conf, context);
		parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
		parserSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

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
		
		// The json object either contains a timestamp field or a date field
		long timestamp;
		if(map.containsKey("date")){
			Date date;
			try {
				date = parserSDF.parse(map.get("date"));
			} catch (ParseException e) {
				log.warn("Failed to parse date:" + map.get("date"));
				return;
			}
			timestamp = date.getTime();
		} else {
			timestamp = Long.parseLong(map.get("timestamp"));
		}

		collector.emit(new Values(
			new LogMessage(map.get("description"), timestamp)
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
