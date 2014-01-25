package org.classified_event_aggregation.storm_input_topology.model.time;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class TimePeriod {
	
	public TimePeriod(String name, String format) {
		this.name = name;
		this.format = format;
	}
	
	private final String name;
	private final String format;
	
	public String getName() {
		return name;
	}
	
	public String convertTimestampToDateFormat(Long timestamp){
		return new DateTime(timestamp).toString(DateTimeFormat.forPattern(this.format));
	}
}
