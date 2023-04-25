package com.commafeed.backend.feed;

import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.SessionFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.model.Feed;

@Singleton
public class FeedQueues {

	private final SessionFactory sessionFactory;
	private final FeedDAO feedDAO;
	private final CommaFeedConfiguration config;
	private final BlockingDeque<FeedRefreshContext> queue = new LinkedBlockingDeque<>();
	private final Meter refill;

	@Inject
	public FeedQueues(SessionFactory sessionFactory, FeedDAO feedDAO, CommaFeedConfiguration config, MetricRegistry metrics) {
		this.sessionFactory = sessionFactory;
		this.config = config;
		this.feedDAO = feedDAO;
		this.refill = metrics.meter(MetricRegistry.name(getClass(), "refill"));

		metrics.register(MetricRegistry.name(getClass(), "queue"), (Gauge<Integer>) queue::size);
	}

	/**
	 * take a feed from the refresh queue
	 */
	public synchronized FeedRefreshContext take() {
		FeedRefreshContext context = queue.poll();
		if (context != null) {
			return context;
		}

		refill();
		try {
			// try to get something from the queue
			// if the queue is empty, wait a bit
			// polling the queue instead of sleeping gives us the opportunity to process a feed immediately if it was added manually with
			// add()
			return queue.poll(15, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException("interrupted while waiting for a feed in the queue", e);
		}
	}

	/**
	 * add a feed to the refresh queue
	 */
	public void add(Feed feed, boolean urgent) {
		if (isFeedAlreadyQueued(feed)) {
			return;
		}

		FeedRefreshContext context = new FeedRefreshContext(feed, urgent);
		if (urgent) {
			queue.addFirst(context);
		} else {
			queue.addLast(context);
		}
	}

	/**
	 * refills the refresh queue
	 */
	private void refill() {
		refill.mark();

		// add feeds that are up to refresh from the database
		int batchSize = Math.min(100, 3 * config.getApplicationSettings().getBackgroundThreads());
		List<Feed> feeds = UnitOfWork.call(sessionFactory, () -> {
			List<Feed> list = feedDAO.findNextUpdatable(batchSize, getLastLoginThreshold());

			// set the disabledDate as we use it in feedDAO.findNextUpdatable() to decide what to refresh next
			Date nextRefreshDate = DateUtils.addMinutes(new Date(), config.getApplicationSettings().getRefreshIntervalMinutes());
			list.forEach(f -> f.setDisabledUntil(nextRefreshDate));
			feedDAO.saveOrUpdate(list);

			return list;
		});

		feeds.forEach(f -> add(f, false));
	}

	public void giveBack(Feed feed) {
		String normalized = FeedUtils.normalizeURL(feed.getUrl());
		feed.setNormalizedUrl(normalized);
		feed.setNormalizedUrlHash(DigestUtils.sha1Hex(normalized));
		feed.setLastUpdated(new Date());
		UnitOfWork.run(sessionFactory, () -> feedDAO.saveOrUpdate(feed));

		// we just finished updating the feed, remove it from the queue
		queue.removeIf(c -> isFeedAlreadyQueued(c.getFeed()));
	}

	private Date getLastLoginThreshold() {
		if (config.getApplicationSettings().getHeavyLoad()) {
			return DateUtils.addDays(new Date(), -30);
		} else {
			return null;
		}
	}

	private boolean isFeedAlreadyQueued(Feed feed) {
		return queue.stream().anyMatch(c -> c.getFeed().getId().equals(feed.getId()));
	}
}
