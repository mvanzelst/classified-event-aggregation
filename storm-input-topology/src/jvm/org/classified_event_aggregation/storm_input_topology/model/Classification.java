package org.classified_event_aggregation.storm_input_topology.model;

public class Classification {

	private final String value;
	private final String key;
	
	public Classification(String key, String value) {
		this.key = key;
		this.value = value;
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
	
	public static Classification fromString(String input){
		if(input.matches("\\A[A-Za-z0-9_\\-]+:[A-Za-z0-9_\\-]+\\Z")){
			String split[] = input.split(":");
			return new Classification(split[0], split[1]);
		} else if(input.matches("\\A[A-Za-z0-9_\\-]+\\Z")){
			return new Classification(input);
		} else {
			throw new RuntimeException("Error parsing classification: " + input);
		}
	}

}
