package org.classified_event_aggregation.storm_input_topology.persistence;

import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.classified_event_aggregation.storm_input_topology.model.Classification;
import org.classified_event_aggregation.storm_input_topology.model.ClassifiedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventStoreCache implements EventStore {

	private final EventStore decoratedEventStore;
	private final Cache cache;
	private static Logger logger = LoggerFactory.getLogger(EventStoreCache.class);

	public EventStoreCache(EventStore decoratedEventStore) {
		this.decoratedEventStore = decoratedEventStore;

		// @todo make this configurable
		CacheConfiguration cacheConfig = new CacheConfiguration();
		cacheConfig.setMaxBytesLocalHeap(1024L * 1024L * 64L); // 64 MB
		cacheConfig.setName(UUID.randomUUID().toString());
		cache = new Cache(cacheConfig);
		CacheManager.create().addCache(cache);
	}

	@Override
	public void beginCommit(Long txid) {
		decoratedEventStore.beginCommit(txid);
	}

	@Override
	public void commit(Long txid) {
		decoratedEventStore.commit(txid);
	}

	@Override
	public void storeClassifiedEvent(ClassifiedEvent event) {
		decoratedEventStore.storeClassifiedEvent(event);
	}

	@Override
	public void setClassificationCounter(String periodTypeName, Long periodStart, String lastDescription, Classification classification, Long amount) {
		logger.trace(String.format("setClassificationCounter(periodTypeName: %s, periodStart: %d, lastDescription: %s, classification: %s, amount: %d)", periodTypeName, periodStart, lastDescription, classification.toString(), amount));
		String cacheKey = periodTypeName + "_" + periodStart + "_" + classification;
		// Store in cache
		Element element = new Element(cacheKey, (long) amount);
		cache.put(element);
		decoratedEventStore.setClassificationCounter(periodTypeName, periodStart, lastDescription, classification, amount);
	}

	@Override
	public Long getClassificationCounter(String periodTypeName, Long periodStart, Classification classification) {
		String cacheKey = periodTypeName + "_" + periodStart + "_" + classification;

		Long output;
		// Try to read from cache
		Element element = cache.get(cacheKey);
		if(element != null){
			output = (long) element.getObjectValue();
		} else {
			output = decoratedEventStore.getClassificationCounter(periodTypeName, periodStart, classification);
			if(output == null){
				return 0L;
			}
			cache.put(new Element(cacheKey, (long) output));
		}
		return output;
	}


}
