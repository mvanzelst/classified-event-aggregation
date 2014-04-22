package org.classified_event_aggregation.test.function;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.classified_event_aggregation.storm_input_topology.LogMessagesAnomalyDetectionTopologyBuilder;
import org.classified_event_aggregation.storm_input_topology.function.ExceptionCountAnomalyDetection;
import org.classified_event_aggregation.storm_input_topology.model.LogMessage;
import org.classified_event_aggregation.storm_input_topology.model.LogSequence;
import org.classified_event_aggregation.test.mock.MockTridentCollector;
import org.junit.Assert;
import org.junit.Test;

import storm.trident.testing.MockTridentTuple;
import storm.trident.tuple.TridentTuple;

import com.google.common.collect.Iterables;

public class ExceptionCountAnomalyDetectionTest {

	@Test
	public void test(){
		ExceptionCountAnomalyDetection function = new ExceptionCountAnomalyDetection();

		MockTridentCollector collector = new MockTridentCollector();

		for (int i = 0; i < 100; i++) {
			LogSequence logSequence = buildCompletedLogSequence(1, 2);
			function.execute(buildLogSequenceTuple(logSequence), collector);
		}

		LogSequence logSequence = buildCompletedLogSequence(1, 25);
		function.execute(buildLogSequenceTuple(logSequence), collector);

		List<Object> lastTuple = Iterables.getLast(collector.getTuples());
		// The relevance of the last notification should be one
		// because the inserted log sequence takes a lot longer than the previous 100
		Assert.assertEquals(lastTuple.get(1), 1);
	}

	private TridentTuple buildLogSequenceTuple(LogSequence logSequence){
		return new MockTridentTuple(
			Arrays.asList(new String[]{"log_sequence"}),
			logSequence
		);
	}

	private LogSequence buildCompletedLogSequence(long start, long end){
		String sequenceId = UUID.randomUUID().toString();
		List<LogMessage> logMessages = Arrays.asList(new LogMessage[]{
			new LogMessage("message1 #SEQUENCE_STATUS:STARTED", start),
			new LogMessage("message2 #SEQUENCE_STATUS:FINISHED", end)
		});
		return new LogSequence("test-app", "a", sequenceId, start, logMessages);
	}
}
