package com.commafeed.backend.feed;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

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

	private SessionFactory sessionFactory;
	private final FeedDAO feedDAO;
	private final CommaFeedConfiguration config;

	private Queue<FeedRefreshContext> addQueue = new ConcurrentLinkedQueue<>();
	private Queue<FeedRefreshContext> takeQueue = new ConcurrentLinkedQueue<>();
	private Queue<Feed> giveBackQueue = new ConcurrentLinkedQueue<>();

	private Meter refill;

	//This constructor ir for testing purpose
    @Inject
    public FeedQueues(Queue<FeedRefreshContext> addQueue, Queue<FeedRefreshContext> takeQueue, Queue<Feed> giveBackQueue ,SessionFactory sessionFactory, FeedDAO feedDAO, CommaFeedConfiguration config, MetricRegistry metrics) {
        this(sessionFactory, feedDAO, config, metrics);

        this.addQueue = addQueue;
        this.takeQueue = takeQueue;
        this.giveBackQueue = giveBackQueue;
    }

	@Inject
	public FeedQueues(SessionFactory sessionFactory, FeedDAO feedDAO, CommaFeedConfiguration config, MetricRegistry metrics) {
		this.sessionFactory = sessionFactory;
		this.config = config;
		this.feedDAO = feedDAO;

		refill = metrics.meter(MetricRegistry.name(getClass(), "refill"));
		metrics.register(MetricRegistry.name(getClass(), "addQueue"), new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return addQueue.size();
			}
		});
		metrics.register(MetricRegistry.name(getClass(), "takeQueue"), new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return takeQueue.size();
			}
		});
		metrics.register(MetricRegistry.name(getClass(), "giveBackQueue"), new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return giveBackQueue.size();
			}
		});
	}

	/**
	 * take a feed from the refresh queue
	 */
	public synchronized FeedRefreshContext take() {
		FeedRefreshContext context = takeQueue.poll();

		if (context == null) {
			refill();
			context = takeQueue.poll();
		}
		return context;
	}

	/**
	 * add a feed to the refresh queue
	 */
	public void add(Feed feed, boolean urgent) {
		int refreshInterval = config.getApplicationSettings().getRefreshIntervalMinutes();
		if (feed.getLastUpdated() == null || feed.getLastUpdated().before(DateUtils.addMinutes(new Date(), -1 * refreshInterval))) {
			boolean alreadyQueued = addQueue.stream().anyMatch(c -> c.getFeed().getId().equals(feed.getId()));
			if (!alreadyQueued) {
				addQueue.add(new FeedRefreshContext(feed, urgent));
			}
		}
	}

	/**
	 * refills the refresh queue and empties the giveBack queue while at it
     *
     * changed private to protected for testing
     */
	protected void refill() {
		refill.mark();

		List<FeedRefreshContext> contexts = new ArrayList<>();
		int batchSize = Math.min(100, 3 * config.getApplicationSettings().getBackgroundThreads());

		// add feeds we got from the add() method
		int addQueueSize = addQueue.size();
		for (int i = 0; i < Math.min(batchSize, addQueueSize); i++) {
			contexts.add(addQueue.poll());
		}

		// add feeds that are up to refresh from the database
		int count = batchSize - contexts.size();
		if (count > 0) {
			List<Feed> feeds = UnitOfWork.call(sessionFactory, () -> feedDAO.findNextUpdatable(count, getLastLoginThreshold()));
			for (Feed feed : feeds) {
				contexts.add(new FeedRefreshContext(feed, false));
			}
		}

		// set the disabledDate as we use it in feedDAO to decide what to refresh next. We also use a map to remove
		// duplicates.
		Map<Long, FeedRefreshContext> map = new LinkedHashMap<>();
		for (FeedRefreshContext context : contexts) {
			Feed feed = context.getFeed();
			feed.setDisabledUntil(DateUtils.addMinutes(new Date(), config.getApplicationSettings().getRefreshIntervalMinutes()));
			map.put(feed.getId(), context);
		}

		// refill the queue
		takeQueue.addAll(map.values());

		// add feeds from the giveBack queue to the map, overriding duplicates
		int giveBackQueueSize = giveBackQueue.size();
		for (int i = 0; i < giveBackQueueSize; i++) {
			Feed feed = giveBackQueue.poll();
			map.put(feed.getId(), new FeedRefreshContext(feed, false));
		}

		// update all feeds in the database
		List<Feed> feeds = map.values().stream().map(c -> c.getFeed()).collect(Collectors.toList());
		UnitOfWork.run(sessionFactory, () -> feedDAO.saveOrUpdate(feeds));
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

	//Changed private to protected for testing
	protected Date getLastLoginThreshold() {
		if (config.getApplicationSettings().getHeavyLoad()) {
			return DateUtils.addDays(new Date(), -30);
		} else {
			return null;
		}
	}

}
