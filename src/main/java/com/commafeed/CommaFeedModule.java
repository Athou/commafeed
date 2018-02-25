package com.commafeed;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.SessionFactory;













import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.MetricFilter;
import com.commafeed.CommaFeedConfiguration.ApplicationSettings;
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

		ApplicationSettings settings = config.getApplicationSettings();

		if (settings.isGraphiteEnabled()) {
			final String graphitePrefix = settings.getGraphitePrefix();
			final String graphiteHost = settings.getGraphiteHost();
			final int graphitePort = settings.getGraphitePort();
			final int graphiteInterval = settings.getGraphiteInterval();

			log.info("Graphite Metrics will be sent to host={}, port={}, prefix={}, interval={}sec", graphiteHost, graphitePort, graphitePrefix, graphiteInterval);

			final Graphite graphite = new Graphite(new InetSocketAddress(graphiteHost, graphitePort));
			final GraphiteReporter reporter = GraphiteReporter.forRegistry(metrics)
			                                                  .prefixedWith(graphitePrefix)
			                                                  .convertRatesTo(TimeUnit.SECONDS)
			                                                  .convertDurationsTo(TimeUnit.MILLISECONDS)
			                                                  .filter(MetricFilter.ALL)
			                                                  .build(graphite);
			reporter.start(graphiteInterval, TimeUnit.SECONDS);
		} else {
			log.info("Graphite Metrics Disabled. Metrics will not be sent.");
		}
	}
}
