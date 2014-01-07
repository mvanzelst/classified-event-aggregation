package org.classified_event_aggregation.service;

public interface EventQueue {
	public void produce(String event);
}
