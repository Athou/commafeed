package com.commafeed.backend.service;

import java.time.Instant;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.feed.FeedEntryKeyword;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.feed.parser.FeedParserResult.Entry;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class FeedEntryService {

	private final FeedSubscriptionDAO feedSubscriptionDAO;
	private final FeedEntryDAO feedEntryDAO;
	private final FeedEntryStatusDAO feedEntryStatusDAO;
	private final FeedEntryContentService feedEntryContentService;
	private final FeedEntryFilteringService feedEntryFilteringService;
	private final CacheService cache;

	/**
	 * this is NOT thread-safe
	 */
	public boolean addEntry(Feed feed, Entry entry, List<FeedSubscription> subscriptions) {
		String guid = FeedUtils.truncate(entry.guid(), 2048);
		String guidHash = DigestUtils.sha1Hex(entry.guid());
		Long existing = feedEntryDAO.findExisting(guidHash, feed);
		if (existing != null) {
			return false;
		}

		FeedEntry feedEntry = buildEntry(feed, entry, guid, guidHash);
		feedEntryDAO.saveOrUpdate(feedEntry);

		// if filter does not match the entry, mark it as read
		for (FeedSubscription sub : subscriptions) {
			boolean matches = true;
			try {
				matches = feedEntryFilteringService.filterMatchesEntry(sub.getFilter(), feedEntry);
			} catch (FeedEntryFilteringService.FeedEntryFilterException e) {
				log.error("could not evaluate filter {}", sub.getFilter(), e);
			}
			if (!matches) {
				FeedEntryStatus status = new FeedEntryStatus(sub.getUser(), sub, feedEntry);
				status.setRead(true);
				feedEntryStatusDAO.saveOrUpdate(status);
			}
		}

		return true;
	}

	private FeedEntry buildEntry(Feed feed, Entry e, String guid, String guidHash) {
		FeedEntry entry = new FeedEntry();
		entry.setGuid(guid);
		entry.setGuidHash(guidHash);
		entry.setUrl(FeedUtils.truncate(e.url(), 2048));
		entry.setUpdated(e.updated());
		entry.setInserted(Instant.now());
		entry.setFeed(feed);

		entry.setContent(feedEntryContentService.findOrCreate(e.content(), feed.getLink()));
		return entry;
	}

	public void markEntry(User user, Long entryId, boolean read) {
		FeedEntry entry = feedEntryDAO.findById(entryId);
		if (entry == null) {
			return;
		}

		FeedSubscription sub = feedSubscriptionDAO.findByFeed(user, entry.getFeed());
		if (sub == null) {
			return;
		}

		FeedEntryStatus status = feedEntryStatusDAO.getStatus(user, sub, entry);
		if (status.isMarkable()) {
			status.setRead(read);
			feedEntryStatusDAO.saveOrUpdate(status);
			cache.invalidateUnreadCount(sub);
			cache.invalidateUserRootCategory(user);
		}
	}

	public void starEntry(User user, Long entryId, Long subscriptionId, boolean starred) {

		FeedSubscription sub = feedSubscriptionDAO.findById(user, subscriptionId);
		if (sub == null) {
			return;
		}

		FeedEntry entry = feedEntryDAO.findById(entryId);
		if (entry == null) {
			return;
		}

		FeedEntryStatus status = feedEntryStatusDAO.getStatus(user, sub, entry);
		status.setStarred(starred);
		feedEntryStatusDAO.saveOrUpdate(status);
	}

	public void markSubscriptionEntries(User user, List<FeedSubscription> subscriptions, Instant olderThan, Instant insertedBefore,
			List<FeedEntryKeyword> keywords) {
		List<FeedEntryStatus> statuses = feedEntryStatusDAO.findBySubscriptions(user, subscriptions, true, keywords, null, -1, -1, null,
				false, false, null, null, null);
		markList(statuses, olderThan, insertedBefore);
		cache.invalidateUnreadCount(subscriptions.toArray(new FeedSubscription[0]));
		cache.invalidateUserRootCategory(user);
	}

	public void markStarredEntries(User user, Instant olderThan, Instant insertedBefore) {
		List<FeedEntryStatus> statuses = feedEntryStatusDAO.findStarred(user, null, -1, -1, null, false);
		markList(statuses, olderThan, insertedBefore);
	}

	private void markList(List<FeedEntryStatus> statuses, Instant olderThan, Instant insertedBefore) {
		List<FeedEntryStatus> statusesToMark = statuses.stream().filter(s -> {
			Instant entryDate = s.getEntry().getUpdated();
			return olderThan == null || entryDate == null || entryDate.isBefore(olderThan);
		}).filter(s -> {
			Instant insertedDate = s.getEntry().getInserted();
			return insertedBefore == null || insertedDate == null || insertedDate.isBefore(insertedBefore);
		}).toList();

		statusesToMark.forEach(s -> s.setRead(true));
		feedEntryStatusDAO.saveOrUpdate(statusesToMark);
	}
}
