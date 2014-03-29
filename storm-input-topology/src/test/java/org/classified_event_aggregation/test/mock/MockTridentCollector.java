package org.classified_event_aggregation.test.mock;

import java.util.ArrayList;
import java.util.List;

import storm.trident.operation.TridentCollector;

public class MockTridentCollector implements TridentCollector {

	private List<List<Object>> tuples = new ArrayList<>();
	
	@Override
	public void emit(List<Object> values) {
		tuples.add(values);
	}

	@Override
	public void reportError(Throwable t) {}
	
	public List<List<Object>> getTuples(){
		return tuples;
	}
}
