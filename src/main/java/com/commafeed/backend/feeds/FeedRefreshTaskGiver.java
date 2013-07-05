package com.commafeed.backend.feeds;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.MetricsBean;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

@ApplicationScoped
public class FeedRefreshTaskGiver {

	protected static final Logger log = LoggerFactory.getLogger(FeedRefreshTaskGiver.class);

	@Inject
	FeedDAO feedDAO;

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Inject
	MetricsBean metricsBean;

	@Inject
	FeedRefreshWorker worker;

	private int backgroundThreads;

	private Queue<Feed> addQueue = Queues.newConcurrentLinkedQueue();
	private Queue<Feed> takeQueue = Queues.newConcurrentLinkedQueue();
	private Queue<Feed> giveBackQueue = Queues.newConcurrentLinkedQueue();

	private ExecutorService executor;

	@PostConstruct
	public void init() {
		backgroundThreads = applicationSettingsService.get()
				.getBackgroundThreads();
		executor = Executors.newFixedThreadPool(1);
	}

	@PreDestroy
	public void shutdown() {
		executor.shutdownNow();
		while (!executor.isTerminated()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("interrupted while waiting for threads to finish.");
			}
		}
	}

	public void start() {
		try {
			// sleeping for a little while, let everything settle
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			log.error("interrupted while sleeping");
		}
		log.info("starting feed refresh task giver");

		executor.execute(new Runnable() {
			@Override
			public void run() {
				while (!executor.isShutdown()) {
					try {
						Feed feed = take();
						if (feed != null) {
							metricsBean.feedRefreshed();
							worker.updateFeed(feed);
						} else {
							log.debug("nothing to do, sleeping for 15s");
							try {
								Thread.sleep(15000);
							} catch (InterruptedException e) {
								log.error("interrupted while sleeping");
							}
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		});
	}

	private Feed take() {
		Feed feed = takeQueue.poll();

		if (feed == null) {
			refill();
			feed = takeQueue.poll();
		}
		return feed;
	}

	public Long getUpdatableCount() {
		return feedDAO.getUpdatableCount(getThreshold());
	}

	private Date getThreshold() {
		boolean heavyLoad = applicationSettingsService.get().isHeavyLoad();
		Date threshold = DateUtils.addMinutes(new Date(), heavyLoad ? -15 : -5);
		return threshold;
	}

	public void add(Feed feed) {
		Date threshold = getThreshold();
		if (feed.getLastUpdated() == null
				|| feed.getLastUpdated().before(threshold)) {
			addQueue.add(feed);
		}
	}

	private void refill() {
		Date now = new Date();

		int count = Math.min(100, 3 * backgroundThreads);
		List<Feed> feeds = null;
		if (applicationSettingsService.get().isCrawlingPaused()) {
			feeds = Lists.newArrayList();
		} else {
			feeds = feedDAO.findNextUpdatable(count, getThreshold());
		}

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
		String normalized = FeedUtils.normalizeURL(feed.getUrl());
		feed.setNormalizedUrl(normalized);
		feed.setNormalizedUrlHash(DigestUtils.sha1Hex(normalized));
		giveBackQueue.add(feed);
	}

}
