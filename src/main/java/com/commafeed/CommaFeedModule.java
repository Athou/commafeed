package com.commafeed;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.SessionFactory;

import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration.CacheType;
import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.cache.NoopCacheService;
import com.commafeed.backend.cache.RedisCacheService;
import com.commafeed.backend.favicon.AbstractFaviconFetcher;
import com.commafeed.backend.favicon.DefaultFaviconFetcher;
import com.commafeed.backend.favicon.FacebookFaviconFetcher;
import com.commafeed.backend.favicon.YoutubeFaviconFetcher;
import com.commafeed.backend.task.OldEntriesCleanupTask;
import com.commafeed.backend.task.OldStatusesCleanupTask;
import com.commafeed.backend.task.OrphanedContentsCleanupTask;
import com.commafeed.backend.task.OrphanedFeedsCleanupTask;
import com.commafeed.backend.task.ScheduledTask;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;

@RequiredArgsConstructor
@Slf4j
public class CommaFeedModule extends AbstractModule {

	@Getter(onMethod = @__({ @Provides }))
	private final SessionFactory sessionFactory;

	@Getter(onMethod = @__({ @Provides }))
	private final CommaFeedConfiguration config;

	@Getter(onMethod = @__({ @Provides }))
	private final MetricRegistry metrics;

	@Override
	protected void configure() {
		CacheService cacheService = config.getApplicationSettings().getCache() == CacheType.NOOP ? new NoopCacheService()
				: new RedisCacheService(config.getRedisPoolFactory().build());
		log.info("using cache {}", cacheService.getClass());
		bind(CacheService.class).toInstance(cacheService);

		Multibinder<AbstractFaviconFetcher> faviconMultibinder = Multibinder.newSetBinder(binder(), AbstractFaviconFetcher.class);
		faviconMultibinder.addBinding().to(YoutubeFaviconFetcher.class);
		faviconMultibinder.addBinding().to(FacebookFaviconFetcher.class);
		faviconMultibinder.addBinding().to(DefaultFaviconFetcher.class);

		Multibinder<ScheduledTask> taskMultibinder = Multibinder.newSetBinder(binder(), ScheduledTask.class);
		taskMultibinder.addBinding().to(OldStatusesCleanupTask.class);
		taskMultibinder.addBinding().to(OldEntriesCleanupTask.class);
		taskMultibinder.addBinding().to(OrphanedFeedsCleanupTask.class);
		taskMultibinder.addBinding().to(OrphanedContentsCleanupTask.class);
	}
}
