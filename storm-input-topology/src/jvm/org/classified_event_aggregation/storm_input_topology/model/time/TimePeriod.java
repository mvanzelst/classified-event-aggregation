package org.classified_event_aggregation.storm_input_topology.model.time;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

public enum TimePeriod {
	YEAR("year", "y"), // 2014
	MONTH("month", "y'-M'MM"), // 2014-M01
	WEEK("week", "x'-W'ww"), // 2014-W04
	DATE("date", "y-MM-dd"), // 2014-01-25
	HOUR("hour", "y-MM-dd 'H-'HH"), // 2014-01-25 H01
	MINUTE("minute", "y-MM-dd HH:mm"), // 2014-01-25 01:15
	SECOND("second", "y-MM-dd HH:mm:ss"); // 2014-01-25 01:15:00
	

	private final String name;
	private final String format;

	private TimePeriod(String name, String format) {
		this.name = name;
		this.format = format;
	}
	
	public String getName(){
		return name;
	}

	public String convertTimestampToDateFormat(Long timestamp) {
		return new DateTime(timestamp)
				.withZone(DateTimeZone.UTC)
				.toString(DateTimeFormat.forPattern(this.format));
	}
	
	/**
	 * @todo Test
	 * 
	 * @param timestamp
	 * @return
	 */
	public Long convertToStartOfPeriod(Long timestamp) {
		TimePeriod p = this;
		DateTime dt = new DateTime(timestamp);
		dt.withZone(DateTimeZone.UTC);
		switch (p){
			case YEAR:
				return dt.withMillisOfDay(0).withDayOfYear(1).getMillis();
			case MONTH:
				return dt.withMillisOfDay(0).withDayOfMonth(1).getMillis();
			case WEEK:
				return dt.withMillisOfDay(0).withDayOfWeek(1).withWeekOfWeekyear(1).getMillis();
			case DATE:
				return dt.withMillisOfDay(0).getMillis();
			case HOUR:
				return dt.withMinuteOfHour(0).withMillisOfSecond(0).withSecondOfMinute(0).getMillis();
			case MINUTE:
				return dt.withMillisOfSecond(0).withSecondOfMinute(0).getMillis();
			case SECOND:
				return dt.withMillisOfSecond(0).getMillis();
			default:
				throw new RuntimeException("Unknown TimePeriod");
		}
	}

}
