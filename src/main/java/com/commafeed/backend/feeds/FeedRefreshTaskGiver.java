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

/**
 * Infinite loop fetching feeds from the database and queuing them to the {@link FeedRefreshWorker} pool. Also handles feed database updates
 * at the end of the cycle through {@link #giveBack(Feed)}.
 * 
 */
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

	private Queue<FeedRefreshContext> addQueue = Queues.newConcurrentLinkedQueue();
	private Queue<FeedRefreshContext> takeQueue = Queues.newConcurrentLinkedQueue();
	private Queue<Feed> giveBackQueue = Queues.newConcurrentLinkedQueue();

	private ExecutorService executor;

	@PostConstruct
	public void init() {
		backgroundThreads = applicationSettingsService.get().getBackgroundThreads();
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
						FeedRefreshContext context = take();
						if (context != null) {
							metricsBean.feedRefreshed();
							worker.updateFeed(context);
						} else {
							log.debug("nothing to do, sleeping for 15s");
							metricsBean.threadWaited();
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

	/**
	 * take a feed from the refresh queue
	 */
	private FeedRefreshContext take() {
		FeedRefreshContext context = takeQueue.poll();

		if (context == null) {
			refill();
			context = takeQueue.poll();
		}
		return context;
	}

	public Long getUpdatableCount() {
		return feedDAO.getUpdatableCount();
	}

	/**
	 * add a feed to the refresh queue
	 */
	public void add(Feed feed, boolean urgent) {
		int refreshInterval = applicationSettingsService.get().getRefreshIntervalMinutes();
		if (feed.getLastUpdated() == null || feed.getLastUpdated().before(DateUtils.addMinutes(new Date(), -1 * refreshInterval))) {
			addQueue.add(new FeedRefreshContext(feed, urgent));
		}
	}

	/**
	 * refills the refresh queue and empties the giveBack queue while at it
	 */
	private void refill() {
		int count = Math.min(300, 3 * backgroundThreads);

		// first, get feeds that are up to refresh from the database
		List<FeedRefreshContext> contexts = Lists.newArrayList();
		if (!applicationSettingsService.get().isCrawlingPaused()) {
			List<Feed> feeds = feedDAO.findNextUpdatable(count);
			for (Feed feed : feeds) {
				contexts.add(new FeedRefreshContext(feed, false));
			}
		}

		// then, add to those the feeds we got from the add() method. We add them at the beginning of the list as they probably have a
		// higher priority
		int size = addQueue.size();
		for (int i = 0; i < size; i++) {
			contexts.add(0, addQueue.poll());
		}

		// set the disabledDate to now as we use the disabledDate in feedDAO to decide what to refresh next. We also use a map to remove
		// duplicates.
		Map<Long, FeedRefreshContext> map = Maps.newLinkedHashMap();
		for (FeedRefreshContext context : contexts) {
			Feed feed = context.getFeed();
			feed.setDisabledUntil(new Date());
			map.put(feed.getId(), context);
		}

		// refill the queue
		takeQueue.addAll(map.values());

		// add feeds from the giveBack queue to the map, overriding duplicates
		size = giveBackQueue.size();
		for (int i = 0; i < size; i++) {
			Feed feed = giveBackQueue.poll();
			map.put(feed.getId(), new FeedRefreshContext(feed, false));
		}

		// update all feeds in the database
		List<Feed> feeds = Lists.newArrayList();
		for (FeedRefreshContext context : map.values()) {
			feeds.add(context.getFeed());
		}
		feedDAO.saveOrUpdate(feeds);
	}

	/**
	 * give a feed back, updating it to the database during the next refill()
	 */
	public void giveBack(Feed feed) {
		String normalized = FeedUtils.normalizeURL(feed.getUrl());
		feed.setNormalizedUrl(normalized);
		feed.setNormalizedUrlHash(DigestUtils.sha1Hex(normalized));
		feed.setLastUpdated(new Date());
		giveBackQueue.add(feed);
	}

}
