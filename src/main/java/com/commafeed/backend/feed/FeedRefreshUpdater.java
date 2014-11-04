package com.commafeed.backend.feed;

import io.dropwizard.lifecycle.Managed;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.SessionFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.CommaFeedConfiguration.ApplicationSettings;
import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.feed.FeedRefreshExecutor.Task;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedUpdateService;
import com.commafeed.backend.service.PubSubService;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Striped;

@Slf4j
@Singleton
public class FeedRefreshUpdater implements Managed {

	private final SessionFactory sessionFactory;
	private final FeedUpdateService feedUpdateService;
	private final PubSubService pubSubService;
	private final FeedQueues queues;
	private final CommaFeedConfiguration config;
	private final FeedSubscriptionDAO feedSubscriptionDAO;
	private final CacheService cache;

	private FeedRefreshExecutor pool;
	private Striped<Lock> locks;

	private Meter entryCacheMiss;
	private Meter entryCacheHit;
	private Meter feedUpdated;
	private Meter entryInserted;

	@Inject
	public FeedRefreshUpdater(SessionFactory sessionFactory, FeedUpdateService feedUpdateService, PubSubService pubSubService,
			FeedQueues queues, CommaFeedConfiguration config, MetricRegistry metrics, FeedSubscriptionDAO feedSubscriptionDAO,
			CacheService cache) {
		this.sessionFactory = sessionFactory;
		this.feedUpdateService = feedUpdateService;
		this.pubSubService = pubSubService;
		this.queues = queues;
		this.config = config;
		this.feedSubscriptionDAO = feedSubscriptionDAO;
		this.cache = cache;

		ApplicationSettings settings = config.getApplicationSettings();
		int threads = Math.max(settings.getDatabaseUpdateThreads(), 1);
		pool = new FeedRefreshExecutor("feed-refresh-updater", threads, Math.min(50 * threads, 1000), metrics);
		locks = Striped.lazyWeakLock(threads * 100000);

		entryCacheMiss = metrics.meter(MetricRegistry.name(getClass(), "entryCacheMiss"));
		entryCacheHit = metrics.meter(MetricRegistry.name(getClass(), "entryCacheHit"));
		feedUpdated = metrics.meter(MetricRegistry.name(getClass(), "feedUpdated"));
		entryInserted = metrics.meter(MetricRegistry.name(getClass(), "entryInserted"));
	}

	@Override
	public void start() throws Exception {
	}

	@Override
	public void stop() throws Exception {
		log.info("shutting down feed refresh updater");
		pool.shutdown();
	}

	public void updateFeed(FeedRefreshContext context) {
		pool.execute(new EntryTask(context));
	}

	private class EntryTask implements Task {

		private FeedRefreshContext context;

		public EntryTask(FeedRefreshContext context) {
			this.context = context;
		}

		@Override
		public void run() {
			boolean ok = true;
			final Feed feed = context.getFeed();
			List<FeedEntry> entries = context.getEntries();
			if (entries.isEmpty()) {
				feed.setMessage("Feed has no entries");
			} else {
				List<String> lastEntries = cache.getLastEntries(feed);
				List<String> currentEntries = Lists.newArrayList();

				List<FeedSubscription> subscriptions = null;
				for (FeedEntry entry : entries) {
					String cacheKey = cache.buildUniqueEntryKey(feed, entry);
					if (!lastEntries.contains(cacheKey)) {
						log.debug("cache miss for {}", entry.getUrl());
						if (subscriptions == null) {
							subscriptions = new UnitOfWork<List<FeedSubscription>>(sessionFactory) {
								@Override
								protected List<FeedSubscription> runInSession() throws Exception {
									return feedSubscriptionDAO.findByFeed(feed);
								}
							}.run();
						}
						ok &= addEntry(feed, entry, subscriptions);
						entryCacheMiss.mark();
					} else {
						log.debug("cache hit for {}", entry.getUrl());
						entryCacheHit.mark();
					}

					currentEntries.add(cacheKey);
				}
				cache.setLastEntries(feed, currentEntries);

				if (subscriptions == null) {
					feed.setMessage("No new entries found");
				}

				if (CollectionUtils.isNotEmpty(subscriptions)) {
					List<User> users = Lists.newArrayList();
					for (FeedSubscription sub : subscriptions) {
						users.add(sub.getUser());
					}
					cache.invalidateUnreadCount(subscriptions.toArray(new FeedSubscription[0]));
					cache.invalidateUserRootCategory(users.toArray(new User[0]));
				}
			}

			if (config.getApplicationSettings().isPubsubhubbub()) {
				handlePubSub(feed);
			}
			if (!ok) {
				// requeue asap
				feed.setDisabledUntil(new Date(0));
			}
			feedUpdated.mark();
			queues.giveBack(feed);
		}

		@Override
		public boolean isUrgent() {
			return context.isUrgent();
		}
	}

	private boolean addEntry(final Feed feed, final FeedEntry entry, final List<FeedSubscription> subscriptions) {
		boolean success = false;

		// lock on feed, make sure we are not updating the same feed twice at
		// the same time
		String key1 = StringUtils.trimToEmpty("" + feed.getId());

		// lock on content, make sure we are not updating the same entry
		// twice at the same time
		FeedEntryContent content = entry.getContent();
		String key2 = DigestUtils.sha1Hex(StringUtils.trimToEmpty(content.getContent() + content.getTitle()));

		Iterator<Lock> iterator = locks.bulkGet(Arrays.asList(key1, key2)).iterator();
		Lock lock1 = iterator.next();
		Lock lock2 = iterator.next();
		boolean locked1 = false;
		boolean locked2 = false;
		try {
			locked1 = lock1.tryLock(1, TimeUnit.MINUTES);
			locked2 = lock2.tryLock(1, TimeUnit.MINUTES);
			if (locked1 && locked2) {
				boolean inserted = new UnitOfWork<Boolean>(sessionFactory) {
					@Override
					protected Boolean runInSession() throws Exception {
						return feedUpdateService.addEntry(feed, entry, subscriptions);
					}
				}.run();
				if (inserted) {
					entryInserted.mark();
				}
				success = true;
			} else {
				log.error("lock timeout for " + feed.getUrl() + " - " + key1);
			}
		} catch (InterruptedException e) {
			log.error("interrupted while waiting for lock for " + feed.getUrl() + " : " + e.getMessage(), e);
		} finally {
			if (locked1) {
				lock1.unlock();
			}
			if (locked2) {
				lock2.unlock();
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
						pubSubService.subscribe(feed);
					}
				}.start();
			}
		}
	}

}
