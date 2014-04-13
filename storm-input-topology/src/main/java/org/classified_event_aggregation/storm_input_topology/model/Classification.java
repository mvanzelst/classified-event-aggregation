package org.classified_event_aggregation.storm_input_topology.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Classification implements Serializable {

	private final String value;
	private final String key;
	private static final Pattern p = Pattern.compile("#([A-Za-z+0-9_-]+):([A-Za-z+0-9_\\-.,]+)"); 
	private static final Logger log = LoggerFactory.getLogger(Classification.class);

	public Classification(String key, String value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public boolean equals(Object obj){
		Classification class2 = (Classification) obj;
		return 
			this.key.contentEquals(class2.key) &&
			this.value.contentEquals(class2.value);
	}

	public String getValue(){
		return value;
	}

	public String getKey(){
		return key;
	}
	
	@Override
	public String toString() {
		return "#" + key + ":" + value;
	}

	public static Map<String, Classification> fromString(String input){
		Matcher m = p.matcher(input);
		Map<String, Classification> classifications = new HashMap<>();
		while(m.find()){
			log.debug("Found group '{}' 1:'{}' 2:'{}'", m.group(), m.group(1), m.group(2));
			Classification c = new Classification(m.group(1), m.group(2));
			classifications.put(c.getKey(), c);
		}
		log.debug("Parsed classifications '{}'", classifications);
		return classifications;
	}
	
}
