package com.commafeed;

import com.codahale.metrics.MetricRegistry;
import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.cache.NoopCacheService;
import com.commafeed.backend.cache.RedisCacheService;
import com.commafeed.config.CommaFeedConfiguration;
import com.commafeed.config.CommaFeedConfiguration.CacheType;
import com.commafeed.config.RedisConfiguration;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

@Singleton
public class CommaFeedProducers {

	@Produces
	@Singleton
	public CacheService cacheService(CommaFeedConfiguration config, RedisConfiguration redis) {
		return config.cache() == CacheType.NOOP ? new NoopCacheService() : new RedisCacheService(redis.build());
	}

	@Produces
	@Singleton
	public MetricRegistry metricRegistry() {
		return new MetricRegistry();
	}
}
