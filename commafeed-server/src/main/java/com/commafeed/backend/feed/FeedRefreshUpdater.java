package com.commafeed.backend.feed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.feed.parser.FeedParserResult.Content;
import com.commafeed.backend.feed.parser.FeedParserResult.Entry;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedEntryService;
import com.commafeed.backend.service.FeedService;
import com.commafeed.frontend.ws.WebSocketMessageBuilder;
import com.commafeed.frontend.ws.WebSocketSessions;
import com.google.common.util.concurrent.Striped;

import io.dropwizard.lifecycle.Managed;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Updates the feed in the database and inserts new entries
 */
@Slf4j
@Singleton
public class FeedRefreshUpdater implements Managed {

	private final UnitOfWork unitOfWork;
	private final FeedService feedService;
	private final FeedEntryService feedEntryService;
	private final FeedSubscriptionDAO feedSubscriptionDAO;
	private final CacheService cache;
	private final WebSocketSessions webSocketSessions;

	private final Striped<Lock> locks;

	private final Meter entryCacheMiss;
	private final Meter entryCacheHit;
	private final Meter feedUpdated;
	private final Meter entryInserted;

	@Inject
	public FeedRefreshUpdater(UnitOfWork unitOfWork, FeedService feedService, FeedEntryService feedEntryService, MetricRegistry metrics,
			FeedSubscriptionDAO feedSubscriptionDAO, CacheService cache, WebSocketSessions webSocketSessions) {
		this.unitOfWork = unitOfWork;
		this.feedService = feedService;
		this.feedEntryService = feedEntryService;
		this.feedSubscriptionDAO = feedSubscriptionDAO;
		this.cache = cache;
		this.webSocketSessions = webSocketSessions;

		locks = Striped.lazyWeakLock(100000);

		entryCacheMiss = metrics.meter(MetricRegistry.name(getClass(), "entryCacheMiss"));
		entryCacheHit = metrics.meter(MetricRegistry.name(getClass(), "entryCacheHit"));
		feedUpdated = metrics.meter(MetricRegistry.name(getClass(), "feedUpdated"));
		entryInserted = metrics.meter(MetricRegistry.name(getClass(), "entryInserted"));
	}

	private AddEntryResult addEntry(final Feed feed, final Entry entry, final List<FeedSubscription> subscriptions) {
		boolean processed = false;
		boolean inserted = false;

		// lock on feed, make sure we are not updating the same feed twice at
		// the same time
		String key1 = StringUtils.trimToEmpty(String.valueOf(feed.getId()));

		// lock on content, make sure we are not updating the same entry
		// twice at the same time
		Content content = entry.content();
		String key2 = DigestUtils.sha1Hex(StringUtils.trimToEmpty(content.content() + content.title()));

		Iterator<Lock> iterator = locks.bulkGet(Arrays.asList(key1, key2)).iterator();
		Lock lock1 = iterator.next();
		Lock lock2 = iterator.next();
		boolean locked1 = false;
		boolean locked2 = false;
		try {
			// try to lock, give up after 1 minute
			locked1 = lock1.tryLock(1, TimeUnit.MINUTES);
			locked2 = lock2.tryLock(1, TimeUnit.MINUTES);
			if (locked1 && locked2) {
				processed = true;
				inserted = unitOfWork.call(() -> feedEntryService.addEntry(feed, entry, subscriptions));
				if (inserted) {
					entryInserted.mark();
				}
			} else {
				log.error("lock timeout for " + feed.getUrl() + " - " + key1);
			}
		} catch (InterruptedException e) {
			log.error("interrupted while waiting for lock for " + feed.getUrl() + " : " + e.getMessage(), e);
		} finally {
			if (locked1) {
				lock1.unlock();
			}
			if (locked2) {
				lock2.unlock();
			}
		}
		return new AddEntryResult(processed, inserted);
	}

	public boolean update(Feed feed, List<Entry> entries) {
		boolean processed = true;
		boolean insertedAtLeastOneEntry = false;

		if (!entries.isEmpty()) {
			List<String> lastEntries = cache.getLastEntries(feed);
			List<String> currentEntries = new ArrayList<>();

			List<FeedSubscription> subscriptions = null;
			for (Entry entry : entries) {
				String cacheKey = cache.buildUniqueEntryKey(entry);
				if (!lastEntries.contains(cacheKey)) {
					log.debug("cache miss for {}", entry.url());
					if (subscriptions == null) {
						subscriptions = unitOfWork.call(() -> feedSubscriptionDAO.findByFeed(feed));
					}
					AddEntryResult addEntryResult = addEntry(feed, entry, subscriptions);
					processed &= addEntryResult.processed;
					insertedAtLeastOneEntry |= addEntryResult.inserted;

					entryCacheMiss.mark();
				} else {
					log.debug("cache hit for {}", entry.url());
					entryCacheHit.mark();
				}

				currentEntries.add(cacheKey);
			}
			cache.setLastEntries(feed, currentEntries);

			if (subscriptions == null) {
				feed.setMessage("No new entries found");
			} else if (insertedAtLeastOneEntry) {
				List<User> users = subscriptions.stream().map(FeedSubscription::getUser).toList();
				cache.invalidateUnreadCount(subscriptions.toArray(new FeedSubscription[0]));
				cache.invalidateUserRootCategory(users.toArray(new User[0]));

				// notify over websocket
				subscriptions.forEach(sub -> webSocketSessions.sendMessage(sub.getUser(), WebSocketMessageBuilder.newFeedEntries(sub)));
			}
		}

		if (!processed) {
			// requeue asap
			feed.setDisabledUntil(new Date(0));
		}

		if (insertedAtLeastOneEntry) {
			feedUpdated.mark();
		}

		unitOfWork.run(() -> feedService.save(feed));

		return processed;
	}

	@AllArgsConstructor
	private static class AddEntryResult {
		private final boolean processed;
		private final boolean inserted;
	}

}
