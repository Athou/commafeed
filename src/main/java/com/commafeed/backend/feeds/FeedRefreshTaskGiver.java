package com.commafeed.backend.feeds;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Queue;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

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

	private Queue<Feed> addQueue = Queues.newConcurrentLinkedQueue();
	private Queue<Feed> takeQueue = Queues.newConcurrentLinkedQueue();
	private Queue<Feed> giveBackQueue = Queues.newConcurrentLinkedQueue();

	@PostConstruct
	public void init() {
		backgroundThreads = applicationSettingsService.get()
				.getBackgroundThreads();
	}

	public void add(Feed feed) {
		Date now = Calendar.getInstance().getTime();
		boolean heavyLoad = applicationSettingsService.get().isHeavyLoad();
		Date threshold = DateUtils.addMinutes(now, heavyLoad ? -10 : -1);
		if (feed.getLastUpdated() == null
				|| feed.getLastUpdated().before(threshold)) {
			feed.setEtagHeader(null);
			feed.setLastModifiedHeader(null);
		}
		addQueue.add(feed);
	}

	public synchronized Feed take() {
		Feed feed = takeQueue.poll();
		if (feed == null) {
			int count = Math.min(100, 5 * backgroundThreads);
			List<Feed> feeds = feedDAO.findNextUpdatable(count);

			int size = addQueue.size();
			for (int i = 0; i < size; i++) {
				feeds.add(addQueue.poll());
			}

			for (Feed f : feeds) {
				takeQueue.add(f);
				f.setLastUpdated(Calendar.getInstance().getTime());
			}

			size = giveBackQueue.size();
			for (int i = 0; i < size; i++) {
				feeds.add(giveBackQueue.poll());
			}

			feedDAO.update(feeds);

			feed = takeQueue.poll();
		}
		metricsBean.feedRefreshed();
		return feed;
	}

	public void giveBack(Feed feed) {
		giveBackQueue.add(feed);
	}

}
