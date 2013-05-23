package com.commafeed.backend.feeds;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.LockMap;
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

@Singleton
public class FeedRefreshUpdater {

	protected static Logger log = LoggerFactory
			.getLogger(FeedRefreshUpdater.class);

	private static LockMap<String> lockMap = new LockMap<String>();

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
		pool = new ThreadPoolExecutor(threads, threads, 0,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
				super.rejectedExecution(r, e);
				log.info("Thread queue full, executing in own thread.");
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

	private void updateEntry(Feed feed, FeedEntry entry,
			List<FeedSubscription> subscriptions) {
		synchronized (lockMap.get(entry.getGuid())) {
			feedUpdateService.updateEntry(feed, entry, subscriptions);
		}
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
