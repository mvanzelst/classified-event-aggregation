package org.classified_event_aggregation.test.topology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.classified_event_aggregation.storm_input_topology.LogMessagesAnomalyDetectionTopologyBuilder;
import org.classified_event_aggregation.storm_input_topology.persistence.LogMessageStore;
import org.classified_event_aggregation.storm_input_topology.persistence.LogMessageStoreStateFactory;
import org.junit.Test;

import storm.trident.operation.TridentCollector;
import storm.trident.spout.IBatchSpout;
import storm.trident.state.State;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.task.IMetricsContext;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;


public class LogMessagesAnomalyDetectionTopologyTest implements Serializable {
	
	@Test
	public void test() throws InterruptedException, IOException {
		LogMessagesAnomalyDetectionTopologyBuilder topologyBuilder = new LogMessagesAnomalyDetectionTopologyBuilder();

		/*
		 *  Insert test data
		 */
		List<Object>[] inputData = retrieveInputData("/data/set1.txt");
		FixedBatchSpoutSynchronized spout = new FixedBatchSpoutSynchronized(new Fields("bytes"), inputData.length, inputData);
		topologyBuilder.setLogRecordSpout(spout);
		
		// Start cluster
		LocalCluster cluster = new LocalCluster();
		Config topologyConf = new Config();
		topologyConf.put("algorithm.duration.sample_size.min", 10);
		topologyConf.put("algorithm.duration.sample_size.max", 100);
		topologyConf.put("algorithm.exception_count.sample_size.min", 10);
		topologyConf.put("algorithm.exception_count.sample_size.max", 100);
		cluster.submitTopology("classifiedEventProcessor", topologyConf, topologyBuilder.buildTopology());
		
		// Wait until all tuples are processed
		do {
			Thread.sleep(200L);
		} while (!FixedBatchSpoutSynchronized.isDone());
		cluster.shutdown();
	}
	
	private List<Object>[] retrieveInputData(String fileName) throws IOException {
		List<Values> tuples = new ArrayList<>();
		InputStream in = this.getClass().getResourceAsStream(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(in), 1024*1024);
		while (br.ready()) {
			String line = br.readLine();
			if(StringUtils.isBlank(line))
				continue;
			
			Values values = new Values(line.getBytes("UTF-8"));
			tuples.add(values);
		}
		return tuples.toArray(new Values[tuples.size()]);
	}
	
	/**
	 * A fixedbatchspout implementation that can be asked if it's done
	 */
	public static class FixedBatchSpoutSynchronized implements IBatchSpout {
		
	    Fields fields;
	    List<Object>[] outputs;
	    int maxBatchSize;
	    HashMap<Long, List<List<Object>>> batches = new HashMap<Long, List<List<Object>>>();
	    
	    // Indicates that an instance of this class is done sending batches
	    private static volatile boolean isDone = false;
	    
	    public FixedBatchSpoutSynchronized(Fields fields, int maxBatchSize, List<Object>... outputs) {
	        this.fields = fields;
	        this.outputs = outputs;
	        this.maxBatchSize = maxBatchSize;
	        this.isDone = false;
	    }
	    
	    int index = 0;
	    boolean cycle = false;
	    
	    public void setCycle(boolean cycle) {
	        this.cycle = cycle;
	    }
	    
	    @Override
	    public void open(Map conf, TopologyContext context) {
	        index = 0;
	    }

	    @Override
	    public void emitBatch(long batchId, TridentCollector collector){
	    	List<List<Object>> batch = this.batches.get(batchId);
	        if(batch == null){
	            batch = new ArrayList<List<Object>>();
	            if(index>=outputs.length && cycle) {
	                index = 0;
	            }
	            for(int i=0; index < outputs.length && i < maxBatchSize; index++, i++) {
	                batch.add(outputs[index]);
	            }
	            this.batches.put(batchId, batch);
	        }
	        
	        // All batches are sent, signal that an instance of this spout is done executing
	        if(batch.size() == 0 && !FixedBatchSpoutSynchronized.isDone)
	        	FixedBatchSpoutSynchronized.isDone = true;
	        
	        for(List<Object> list : batch){
	            collector.emit(list);
	        }
	    }
	    
	    /**
	     * TODO: The spouts are serialized by storm when building a cluster. There must be
	     * a nicer way to find out if a spout is done. This only works if all the spouts are running
	     * in the same jvm. Which is always true when running a test
	     */
	    public static boolean isDone(){
	    	return FixedBatchSpoutSynchronized.isDone;
	    }

	    @Override
	    public void ack(long batchId) {
	        this.batches.remove(batchId);
	    }

	    @Override
	    public void close() {
	    }

	    @Override
	    public Map getComponentConfiguration() {
	        Config conf = new Config();
	        conf.setMaxTaskParallelism(1);
	        return conf;
	    }

	    @Override
	    public Fields getOutputFields() {
	        return fields;
	    }
	}
	
	public class LogMessageStoreStateFactoryMock extends LogMessageStoreStateFactory {
		
		@Override
		public State makeState(Map conf, IMetricsContext metrics,
				int partitionIndex, int numPartitions) {

			LogMessageStore.Config config = new LogMessageStore.Config();
			config.keySpace = "cea_test";
			config.node = "localhost";
			return new LogMessageStore(config);
		}
		
	}
}
