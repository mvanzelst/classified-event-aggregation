package org.classified_event_aggregation.storm_input_topology.model;

public class Classification {

	private final String value;
	private final String key;
	
	public Classification(String value, String key) {
		this.value = value;
		this.key = key;
	}
	
	public Classification(String value) {
		this.value = value;
		this.key = "";
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
		if(key.isEmpty()){
			return "#" + value;
		} else {
			return "#" + key + ":" + value;
		}
	}

}
