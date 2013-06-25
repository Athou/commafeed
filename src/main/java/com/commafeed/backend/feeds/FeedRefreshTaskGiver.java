package com.commafeed.backend.feeds;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.time.DateUtils;

import com.commafeed.backend.MetricsBean;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.google.api.client.util.Maps;
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
		Date now = new Date();
		boolean heavyLoad = applicationSettingsService.get().isHeavyLoad();
		Date threshold = DateUtils.addMinutes(now, heavyLoad ? -10 : -1);
		if (feed.getLastUpdated() == null
				|| feed.getLastUpdated().before(threshold)) {
			addQueue.add(feed);
		}
	}

	public synchronized Feed take() {
		Feed feed = takeQueue.poll();

		if (feed == null) {
			refill();
			feed = takeQueue.poll();
		}

		if (feed != null) {
			metricsBean.feedRefreshed();
		}
		return feed;
	}

	private void refill() {
		Date now = new Date();

		int count = 3 * backgroundThreads;
		List<Feed> feeds = feedDAO.findNextUpdatable(count);

		int size = addQueue.size();
		for (int i = 0; i < size; i++) {
			feeds.add(0, addQueue.poll());
		}

		Map<Long, Feed> map = Maps.newLinkedHashMap();
		for (Feed f : feeds) {
			f.setLastUpdated(now);
			map.put(f.getId(), f);
		}
		takeQueue.addAll(map.values());

		size = giveBackQueue.size();
		for (int i = 0; i < size; i++) {
			Feed f = giveBackQueue.poll();
			f.setLastUpdated(now);
			map.put(f.getId(), f);
		}

		feedDAO.saveOrUpdate(map.values());
	}

	public void giveBack(Feed feed) {
		giveBackQueue.add(feed);
	}

}
