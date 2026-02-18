package com.commafeed.backend.feed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import jakarta.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.commafeed.backend.Digests;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.feed.parser.FeedParserResult.Content;
import com.commafeed.backend.feed.parser.FeedParserResult.Entry;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.Models;
import com.commafeed.backend.service.FeedEntryService;
import com.commafeed.backend.service.FeedService;
import com.google.common.util.concurrent.Striped;

import lombok.extern.slf4j.Slf4j;

/**
 * Updates the feed in the database and inserts new entries
 */
@Slf4j
@Singleton
public class FeedRefreshUpdater {

	private final UnitOfWork unitOfWork;
	private final FeedService feedService;
	private final FeedEntryService feedEntryService;
	private final FeedSubscriptionDAO feedSubscriptionDAO;

	private final Striped<Lock> locks;

	private final Meter feedUpdated;
	private final Meter entryInserted;

	public FeedRefreshUpdater(UnitOfWork unitOfWork, FeedService feedService, FeedEntryService feedEntryService, MetricRegistry metrics,
			FeedSubscriptionDAO feedSubscriptionDAO) {
		this.unitOfWork = unitOfWork;
		this.feedService = feedService;
		this.feedEntryService = feedEntryService;
		this.feedSubscriptionDAO = feedSubscriptionDAO;

		locks = Striped.lazyWeakLock(100000);

		feedUpdated = metrics.meter(MetricRegistry.name(getClass(), "feedUpdated"));
		entryInserted = metrics.meter(MetricRegistry.name(getClass(), "entryInserted"));
	}

	private AddEntryResult addEntry(final Feed feed, final Entry entry, final List<FeedSubscription> subscriptions) {
		boolean processed = false;
		FeedEntry insertedEntry = null;
		Set<FeedSubscription> subscriptionsForWhichEntryIsUnread = new HashSet<>();

		// lock on feed, make sure we are not updating the same feed twice at
		// the same time
		String key1 = StringUtils.trimToEmpty(String.valueOf(feed.getId()));

		// lock on content, make sure we are not updating the same entry
		// twice at the same time
		Content content = entry.content();
		String key2 = Digests.sha1Hex(StringUtils.trimToEmpty(content.content() + content.title()));

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
				insertedEntry = unitOfWork.call(() -> {
					if (feedEntryService.find(feed, entry) != null) {
						// entry already exists, nothing to do
						return null;
					}

					FeedEntry feedEntry = feedEntryService.create(feed, entry);
					entryInserted.mark();
					for (FeedSubscription sub : subscriptions) {
						boolean unread = feedEntryService.applyFilter(sub, feedEntry);
						if (unread) {
							subscriptionsForWhichEntryIsUnread.add(sub);
						}
					}
					return feedEntry;
				});
			} else {
				log.error("lock timeout for {} - {}", feed.getUrl(), key1);
			}
		} catch (InterruptedException e) {
			log.error("interrupted while waiting for lock for {} : {}", feed.getUrl(), e.getMessage(), e);
			Thread.currentThread().interrupt();
		} finally {
			if (locked1) {
				lock1.unlock();
			}
			if (locked2) {
				lock2.unlock();
			}
		}
		return new AddEntryResult(processed, insertedEntry, subscriptionsForWhichEntryIsUnread);
	}

	public FeedRefreshUpdaterResult update(Feed feed, List<Entry> entries) {
		boolean processed = true;
		long inserted = 0;
		Map<FeedSubscription, List<FeedEntry>> insertedUnreadEntriesBySubscription = new HashMap<>();

		if (!entries.isEmpty()) {
			List<FeedSubscription> subscriptions = null;
			for (Entry entry : entries) {
				if (subscriptions == null) {
					subscriptions = unitOfWork.call(() -> feedSubscriptionDAO.findByFeed(feed));
				}
				AddEntryResult addEntryResult = addEntry(feed, entry, subscriptions);
				processed &= addEntryResult.processed;
				inserted += addEntryResult.insertedEntry != null ? 1 : 0;
				addEntryResult.subscriptionsForWhichEntryIsUnread.forEach(sub -> {
					if (addEntryResult.insertedEntry != null) {
						insertedUnreadEntriesBySubscription.computeIfAbsent(sub, k -> new ArrayList<>()).add(addEntryResult.insertedEntry);
					}
				});
			}

			if (inserted == 0) {
				feed.setMessage("No new entries found");
			} else if (inserted > 0) {
				feed.setMessage("Found %s new entries".formatted(inserted));
			}
		}

		if (!processed) {
			// requeue asap
			feed.setDisabledUntil(Models.MINIMUM_INSTANT);
		}

		if (inserted > 0) {
			feedUpdated.mark();
		}

		unitOfWork.run(() -> feedService.update(feed));

		return new FeedRefreshUpdaterResult(insertedUnreadEntriesBySubscription);
	}

	private record AddEntryResult(boolean processed, FeedEntry insertedEntry, Set<FeedSubscription> subscriptionsForWhichEntryIsUnread) {
	}

	public record FeedRefreshUpdaterResult(Map<FeedSubscription, List<FeedEntry>> insertedUnreadEntriesBySubscription) {
	}

}
