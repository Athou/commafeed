package com.commafeed.backend.feeds;

import java.util.Calendar;
import java.util.List;
import java.util.Queue;

import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;

import com.commafeed.backend.MetricsBean;
import com.commafeed.backend.StartupBean;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.google.common.collect.Queues;

@Singleton
public class FeedRefreshTaskGiver {

	@Inject
	FeedDAO feedDAO;

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Inject
	StartupBean startupBean;

	@Inject
	MetricsBean metricsBean;

	private Queue<Feed> queue = Queues.newConcurrentLinkedQueue();

	@Lock(LockType.WRITE)
	public void add(Feed feed) {
		queue.add(feed);
		feed.setLastUpdated(Calendar.getInstance().getTime());
		feedDAO.update(feed);
	}

	@Lock(LockType.WRITE)
	public Feed take() {
		if (queue.peek() == null) {
			List<Feed> feeds = feedDAO
					.findNextUpdatable(30 * applicationSettingsService.get()
							.getBackgroundThreads());
			for (Feed feed : feeds) {
				queue.add(feed);
				feed.setLastUpdated(Calendar.getInstance().getTime());
			}
			feedDAO.update(feeds);
		}
		metricsBean.feedRefreshed();
		return queue.poll();
	}

	@PreDestroy
	public void shutdown() {
		startupBean.shutdown();
	}
}
