package org.classified_event_aggregation.storm_input_topology.function;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.classified_event_aggregation.storm_input_topology.DurationAnomalyDetectionTopology;
import org.classified_event_aggregation.storm_input_topology.model.Classification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.trident.operation.Function;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.TridentOperationContext;
import storm.trident.tuple.TridentTuple;
import backtype.storm.tuple.Values;


/**
 * @TODO this probably can be done a lot nicer
 * @author marijn
 *
 */
@SuppressWarnings("serial")
public class TaskDurationDetection implements Function {
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(TaskDurationDetection.class);

	private Cache cache = null;

	private int taskMaxAge;
	private ScheduledExecutorService executor;

	@Override
	public void prepare(Map conf, TridentOperationContext context) {
		// @TODO make this configurable
		CacheConfiguration cacheConfig = new CacheConfiguration();
		cacheConfig.setMaxBytesLocalHeap(1024L * 1024L * 64L); // 64 MB
		cacheConfig.setName(UUID.randomUUID().toString());
		cache = new Cache(cacheConfig);
		CacheManager.create().addCache(cache);

		taskMaxAge = 20000;
		executor = Executors.newScheduledThreadPool(1);
		final TaskDurationDetection ref = this;
		executor.scheduleAtFixedRate(new Runnable() {

			@SuppressWarnings("unused")
			private final Logger log = LoggerFactory.getLogger(DurationAnomalyDetectionTopology.class);

			@Override
			public void run() {
				log.trace("Tick");
				try {
					ref.tick();
				} catch (Exception e){
					log.error("Failed to execute tick", e);
				}
				log.trace("Done tick");
			}
		}, 0L, 2000L, TimeUnit.MILLISECONDS);
	}

	@Override
	public void cleanup() {
		cache = null;
		try {
			executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException("Failed to stop TaskDurationAnomolyDetection", e);
		}
	}

	@Override
	public void execute(TridentTuple tuple, TridentCollector collector) {
		Collection<Classification> classifications = (Collection<Classification>) tuple.getValueByField("classifications");
		String taskName = null, taskId = null, taskStatus = null;
		for (Classification classification : classifications) {
			if(classification.getKey().contentEquals("TASK_ID")){
				taskId = classification.getValue();
			} else if(classification.getKey().contentEquals("TASK_NAME")){
				taskName = classification.getValue();
			} else if(classification.getKey().contentEquals("TASK_STATUS")){
				taskStatus = classification.getValue();
			}
		}
		// The logrecord doesn't contain a starting or done tag for the TASK_STATUS
		if(taskStatus == null){
			return;
		}

		if(taskStatus.contentEquals("STARTING")){
			startTaskEvent(taskName, taskId, tuple.getLongByField("timestamp"));
		} else if(taskStatus.contentEquals("DONE")){
			finishedTaskEvent(taskName, taskId, tuple.getLongByField("timestamp"));
		} else {
			log.warn("Unrecognized taskStatus '{}'", taskStatus);
		}

		// Emit durations
		for (Map<String, Object> task : taskDurationEmitQueue){
			//"task_name", "duration", "start_timestamp", "end_timestamp"
			collector.emit(new Values(task.get("task_name"), task.get("duration"), task.get("start_timestamp"), task.get("end_timestamp"), task.get("task_id")));
		}
	}

	private Map<String, Map<String, Object>> startTaskEvents = new ConcurrentHashMap<>();
	private Map<String, Map<String, Object>> finishedTasksEvents = new ConcurrentHashMap<>();
	private Map<String, DescriptiveStatistics> durationsPerTask = new ConcurrentHashMap<>();
	private Queue<Map<String, Object>> taskDurationEmitQueue = new ConcurrentLinkedQueue<>();

	private void startTaskEvent(String taskName, String taskId, long timestamp){
		Map<String, Object> taskInfo = new HashMap<>();
		taskInfo.put("taskName", taskName);
		taskInfo.put("timestamp", timestamp);
		startTaskEvents.put(taskId, taskInfo);

		log.debug("Found start of task event, taskName '{}', taskId {} ", taskName, taskId);
	}

	private void finishedTaskEvent(String taskName, String taskId, long timestamp){
		Map<String, Object> taskInfo = new HashMap<>();
		taskInfo.put("taskName", taskName);
		taskInfo.put("timestamp", timestamp);
		finishedTasksEvents.put(taskId, taskInfo);

		log.debug("Found end of task event, taskName '{}', taskId {} ", taskName, taskId);
	}

	public synchronized void tick(){
		if(log.isDebugEnabled()){
			log.debug("startTaskEvents has items {}", startTaskEvents.entrySet().iterator().hasNext());
			log.debug("finishedTasksEvents has items {}", finishedTasksEvents.entrySet().iterator().hasNext());
		}
		// Check for each started task if there is a matching finished task
		for (Iterator<Entry<String, Map<String, Object>>> iter = startTaskEvents.entrySet().iterator(); iter.hasNext();) {
			Entry<String, Map<String, Object>> entry = iter.next();
			if(finishedTasksEvents.containsKey(entry.getKey())){
				// We've found a finished task
				Map<String, Object> startedTaskInfo = entry.getValue();
				String taskName = (String) startedTaskInfo.get("taskName");
				String taskId = entry.getKey();
				Map<String, Object> finishedTaskInfo = finishedTasksEvents.get(taskId);

				// Calculate the duration of the task
				long duration = (long) finishedTaskInfo.get("timestamp") - (long) startedTaskInfo.get("timestamp");

				// Print duration
				log.info("Task '{}' with id '{}' was completed after {} ms", taskName, taskId, duration);

				HashMap<String, Object> map = new HashMap<>();
				map.put("task_name", taskName);
				map.put("task_id", taskId);
				map.put("duration", duration);
				map.put("start_timestamp", (long) startedTaskInfo.get("timestamp"));
				map.put("end_timestamp", (long) finishedTaskInfo.get("timestamp"));

				// Emit duration
				taskDurationEmitQueue.add(map);

				// Delete the processed task from both maps
				finishedTasksEvents.remove(taskId);
				iter.remove();
			}
		}

		// Remove start events older than ${task_max_age} ms
		for (Iterator<Entry<String, Map<String, Object>>> iter = startTaskEvents.entrySet().iterator(); iter.hasNext();) {
			Entry<String, Map<String, Object>> entry = iter.next();
			long age = System.currentTimeMillis() - (long) entry.getValue().get("timestamp");
			if(age > taskMaxAge){
				Map<String, Object> taskInfo = entry.getValue();

				HashMap<String, Object> map = new HashMap<>();
				map.put("task_name", taskInfo.get("name"));
				map.put("task_id", entry.getKey());
				map.put("duration", age);
				map.put("start_timestamp", (long) taskInfo.get("timestamp"));
				map.put("end_timestamp", System.currentTimeMillis());

				// Emit duration
				taskDurationEmitQueue.add(map);

				iter.remove();
				// @TODO store this
				log.warn("Task '{}' with id '{}' expired after {} ms", entry.getValue().get("taskName"), entry.getKey(), age);
			}
		}

		// Remove finished events older than ${task_max_age} ms
		for (Iterator<Entry<String, Map<String, Object>>> iter = finishedTasksEvents.entrySet().iterator(); iter.hasNext();) {
			Entry<String, Map<String, Object>> entry = iter.next();
			long age = System.currentTimeMillis() - (long) entry.getValue().get("timestamp");
			if(age > taskMaxAge){
				iter.remove();
				// @TODO store this
				log.error("Task '{}' with id '{}' was not started after receiving task completion message {} ms ago, this should not happen!", entry.getValue().get("taskName"), entry.getValue().get("taskId"), age);
			}
		}
	}
}
