package com.commafeed;

import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration.Redis;
import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.cache.NoopCacheService;
import com.commafeed.backend.cache.RedisCacheService;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

@Singleton
public class CommaFeedProducers {

	@Produces
	@Singleton
	public CacheService cacheService(CommaFeedConfiguration config) {
		Redis redis = config.redis();
		if (redis.host().isEmpty()) {
			return new NoopCacheService();
		}

		return new RedisCacheService(redis.build());
	}

	@Produces
	@Singleton
	public MetricRegistry metricRegistry() {
		return new MetricRegistry();
	}
}
