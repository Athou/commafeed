package com.commafeed.backend.feeds;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.ApplicationSettings;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedPushInfo;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.pubsubhubbub.SubscriptionHandler;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.backend.services.FeedUpdateService;

import de.jkeylockmanager.manager.KeyLockManager;
import de.jkeylockmanager.manager.KeyLockManagers;
import de.jkeylockmanager.manager.LockCallback;

@Singleton
public class FeedRefreshUpdater {

	protected static Logger log = LoggerFactory
			.getLogger(FeedRefreshUpdater.class);

	private static final KeyLockManager lockManager = KeyLockManagers.newLock();

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
	FeedSubscriptionDAO feedSubscriptionDAO;

	private ThreadPoolExecutor pool;

	@PostConstruct
	public void init() {
		ApplicationSettings settings = applicationSettingsService.get();
		int threads = Math.max(settings.getDatabaseUpdateThreads(), 1);
		log.info("Creating database pool with {} threads", threads);
		pool = new ThreadPoolExecutor(threads, threads, 0,
				TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
						100 * threads));
		pool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
				log.info("Thread queue full, waiting...");
				try {
					e.getQueue().put(r);
				} catch (InterruptedException e1) {
					log.error("Interrupted while waiting for queue.", e);
				}
			}
		});
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
			if (entries != null) {
				List<FeedSubscription> subscriptions = feedSubscriptionDAO
						.findByFeed(feed);
				for (FeedEntry entry : entries) {
					updateEntry(feed, entry, subscriptions);
				}
			}

			if (applicationSettingsService.get().isPubsubhubbub()) {
				handlePubSub(feed);
			}
			taskGiver.giveBack(feed);
		}

	}

	private void updateEntry(final Feed feed, final FeedEntry entry,
			final List<FeedSubscription> subscriptions) {
		lockManager.executeLocked(entry.getGuid(), new LockCallback() {
			@Override
			public void doInLock() throws Exception {
				feedUpdateService.updateEntry(feed, entry, subscriptions);
			}
		});
	}

	private void handlePubSub(final Feed feed) {
		FeedPushInfo info = feed.getPushInfo();
		if (info != null && info.isActive() == false) {
			new Thread() {
				@Override
				public void run() {
					handler.subscribe(feed);
				}
			}.start();
		}
	}

}
