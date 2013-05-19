package com.commafeed.backend.feeds;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Queue;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.apache.commons.lang3.time.DateUtils;

import com.commafeed.backend.MetricsBean;
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
	MetricsBean metricsBean;

	private int backgroundThreads;
	private Queue<Feed> queue = Queues.newConcurrentLinkedQueue();

	@PostConstruct
	public void init() {
		backgroundThreads = applicationSettingsService.get()
				.getBackgroundThreads();
	}

	@Lock(LockType.WRITE)
	public void add(Feed feed) {
		Date now = Calendar.getInstance().getTime();
		Date tenMinutesAgo = DateUtils.addMinutes(now, -10);
		if (feed.getLastUpdated() == null
				|| feed.getLastUpdated().before(tenMinutesAgo)) {
			feed.setEtagHeader(null);
			feed.setLastModifiedHeader(null);
		}

		queue.add(feed);
		feed.setLastUpdated(Calendar.getInstance().getTime());
		feedDAO.update(feed);
	}

	@Lock(LockType.WRITE)
	public Feed take() {
		Feed feed = queue.poll();
		if (feed == null) {
			int count = Math.min(100, 5 * backgroundThreads);
			List<Feed> feeds = feedDAO.findNextUpdatable(count);
			for (Feed f : feeds) {
				queue.add(f);
				f.setLastUpdated(Calendar.getInstance().getTime());
			}
			feedDAO.update(feeds);
			feed = queue.poll();
		}
		metricsBean.feedRefreshed();
		return feed;
	}

}
