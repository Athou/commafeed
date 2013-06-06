package com.commafeed.backend.feeds;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.MetricsBean;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.ApplicationSettings;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.pubsubhubbub.SubscriptionHandler;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.backend.services.FeedUpdateService;
import com.google.common.util.concurrent.Striped;

@Singleton
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

	private ThreadPoolExecutor pool;
	private BlockingQueue<Runnable> queue;
	private Striped<Lock> locks;

	@PostConstruct
	public void init() {
		ApplicationSettings settings = applicationSettingsService.get();
		int threads = Math.max(settings.getDatabaseUpdateThreads(), 1);
		log.info("Creating database pool with {} threads", threads);
		locks = Striped.lazyWeakLock(threads * 1000);
		pool = new ThreadPoolExecutor(threads, threads, 0,
				TimeUnit.MILLISECONDS,
				queue = new ArrayBlockingQueue<Runnable>(500 * threads));
		pool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
				log.debug("Thread queue full, waiting...");
				try {
					e.getQueue().put(r);
				} catch (InterruptedException e1) {
					log.error("Interrupted while waiting for queue.", e);
				}
			}
		});
	}

	@PreDestroy
	public void shutdown() {
		pool.shutdownNow();
		while (!pool.isTerminated()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("interrupted while waiting for threads to finish.");
			}
		}
	}

	public void updateFeed(Feed feed, Collection<FeedEntry> entries) {
		pool.execute(new Task(feed, entries));
	}

	private class Task implements Runnable {

		private Feed feed;
		private Collection<FeedEntry> entries;

		public Task(Feed feed, Collection<FeedEntry> entries) {
			this.feed = feed;
			this.entries = entries;
		}

		@Override
		public void run() {
			boolean ok = true;
			if (entries.isEmpty() == false) {
				List<FeedSubscription> subscriptions = feedSubscriptionDAO
						.findByFeed(feed);
				for (FeedEntry entry : entries) {
					ok &= updateEntry(feed, entry, subscriptions);
				}
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
	}

	private boolean updateEntry(final Feed feed, final FeedEntry entry,
			final List<FeedSubscription> subscriptions) {
		String key = StringUtils.trimToEmpty(entry.getGuid() + entry.getUrl());
		Lock lock = locks.get(key);
		boolean locked = false;
		try {
			locked = lock.tryLock(1, TimeUnit.MINUTES);
			if (locked) {
				feedUpdateService.updateEntry(feed, entry, subscriptions);
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
		return locked;
	}

	private void handlePubSub(final Feed feed) {
		if (feed.getPushHub() != null && feed.getPushTopic() != null) {
			Date lastPing = feed.getPushLastPing();
			Date now = Calendar.getInstance().getTime();
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
		return queue.size();
	}

}
