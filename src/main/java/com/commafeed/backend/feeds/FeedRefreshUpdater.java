package com.commafeed.backend.feeds;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.MetricsBean;
import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.feeds.FeedRefreshExecutor.Task;
import com.commafeed.backend.model.ApplicationSettings;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.pubsubhubbub.SubscriptionHandler;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.backend.services.FeedUpdateService;
import com.google.api.client.util.Lists;
import com.google.common.util.concurrent.Striped;

@ApplicationScoped
public class FeedRefreshUpdater {

	protected static Logger log = LoggerFactory
			.getLogger(FeedRefreshUpdater.class);

	@Inject
	FeedUpdateService feedUpdateService;

	@Inject
	SubscriptionHandler handler;

	@Inject
	FeedRefreshTaskGiver taskGiver;

	@Inject
	FeedDAO feedDAO;

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Inject
	MetricsBean metricsBean;

	@Inject
	FeedSubscriptionDAO feedSubscriptionDAO;

	@Inject
	FeedEntryDAO feedEntryDAO;

	@Inject
	CacheService cache;

	private FeedRefreshExecutor pool;
	private Striped<Lock> locks;

	@PostConstruct
	public void init() {
		ApplicationSettings settings = applicationSettingsService.get();
		int threads = Math.max(settings.getDatabaseUpdateThreads(), 1);
		pool = new FeedRefreshExecutor("feed-refresh-updater", threads, 500 * threads);
		locks = Striped.lazyWeakLock(threads * 100000);
	}

	@PreDestroy
	public void shutdown() {
		pool.shutdown();
	}

	public void updateFeed(Feed feed, Collection<FeedEntry> entries) {
		pool.execute(new EntryTask(feed, entries));
	}

	private class EntryTask implements Task {

		private Feed feed;
		private Collection<FeedEntry> entries;

		public EntryTask(Feed feed, Collection<FeedEntry> entries) {
			this.feed = feed;
			this.entries = entries;
		}

		@Override
		public void run() {
			boolean ok = true;
			if (entries.isEmpty() == false) {

				List<String> lastEntries = cache.getLastEntries(feed);
				List<String> currentEntries = Lists.newArrayList();

				List<FeedSubscription> subscriptions = null;
				for (FeedEntry entry : entries) {
					String cacheKey = cache.buildUniqueEntryKey(feed, entry);
					if (!lastEntries.contains(cacheKey)) {
						log.debug("cache miss for {}", entry.getUrl());
						if (subscriptions == null) {
							subscriptions = feedSubscriptionDAO
									.findByFeed(feed);
						}
						ok &= updateEntry(feed, entry, subscriptions);
						metricsBean.entryCacheMiss();
					} else {
						log.debug("cache hit for {}", entry.getUrl());
						metricsBean.entryCacheHit();
					}
					currentEntries.add(cacheKey);
				}
				cache.setLastEntries(feed, currentEntries);
			}

			if (applicationSettingsService.get().isPubsubhubbub()) {
				handlePubSub(feed);
			}
			if (!ok) {
				feed.setDisabledUntil(null);
			}
			metricsBean.feedUpdated();
			taskGiver.giveBack(feed);
		}

		@Override
		public boolean isUrgent() {
			return feed.isUrgent();
		}
	}

	private boolean updateEntry(final Feed feed, final FeedEntry entry,
			final List<FeedSubscription> subscriptions) {
		boolean success = false;

		String key = StringUtils.trimToEmpty(entry.getGuid() + entry.getUrl());
		Lock lock = locks.get(key);
		boolean locked = false;
		try {
			locked = lock.tryLock(1, TimeUnit.MINUTES);
			if (locked) {
				feedUpdateService.updateEntry(feed, entry, subscriptions);
				success = true;
			} else {
				log.error("lock timeout for " + feed.getUrl() + " - " + key);
			}
		} catch (InterruptedException e) {
			log.error("interrupted while waiting for lock for " + feed.getUrl()
					+ " : " + e.getMessage(), e);
		} finally {
			if (locked) {
				lock.unlock();
			}
		}
		return success;
	}

	private void handlePubSub(final Feed feed) {
		if (feed.getPushHub() != null && feed.getPushTopic() != null) {
			Date lastPing = feed.getPushLastPing();
			Date now = new Date();
			if (lastPing == null || lastPing.before(DateUtils.addDays(now, -3))) {
				new Thread() {
					@Override
					public void run() {
						handler.subscribe(feed);
					}
				}.start();
			}
		}
	}

	public int getQueueSize() {
		return pool.getQueueSize();
	}

	public int getActiveCount() {
		return pool.getActiveCount();
	}

}
