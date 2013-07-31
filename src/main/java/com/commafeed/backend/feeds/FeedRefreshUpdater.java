package com.commafeed.backend.feeds;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
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
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.pubsubhubbub.SubscriptionHandler;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.backend.services.FeedUpdateService;
import com.google.api.client.util.Lists;
import com.google.common.util.concurrent.Striped;

@ApplicationScoped
public class FeedRefreshUpdater {

	protected static Logger log = LoggerFactory.getLogger(FeedRefreshUpdater.class);

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
		pool = new FeedRefreshExecutor("feed-refresh-updater", threads, Math.min(50 * threads, 1000));
		locks = Striped.lazyWeakLock(threads * 100000);
	}

	@PreDestroy
	public void shutdown() {
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
			Feed feed = context.getFeed();
			List<FeedEntry> entries = context.getEntries();
			if (entries.isEmpty() == false) {

				List<String> lastEntries = cache.getLastEntries(feed);
				List<String> currentEntries = Lists.newArrayList();

				List<FeedSubscription> subscriptions = null;
				for (FeedEntry entry : entries) {
					String cacheKey = cache.buildUniqueEntryKey(feed, entry);
					if (!lastEntries.contains(cacheKey)) {
						log.debug("cache miss for {}", entry.getUrl());
						if (subscriptions == null) {
							subscriptions = feedSubscriptionDAO.findByFeed(feed);
						}
						ok &= addEntry(feed, entry, subscriptions);
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
				// requeue asap
				feed.setDisabledUntil(new Date(0));
			}
			metricsBean.feedUpdated();
			taskGiver.giveBack(feed);
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
				feedUpdateService.updateEntry(feed, entry);
				List<User> users = Lists.newArrayList();
				for (FeedSubscription sub : subscriptions) {
					users.add(sub.getUser());
				}
				cache.invalidateUnreadCount(subscriptions.toArray(new FeedSubscription[0]));
				cache.invalidateUserRootCategory(users.toArray(new User[0]));
				metricsBean.entryInserted();
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
