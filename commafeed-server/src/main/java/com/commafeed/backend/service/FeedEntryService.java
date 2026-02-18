package com.commafeed.backend.service;

import java.time.Instant;
import java.util.List;

import jakarta.inject.Singleton;

import com.commafeed.backend.Digests;
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
import com.commafeed.backend.service.FeedEntryFilteringService.FeedEntryFilterException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Singleton
public class FeedEntryService {

	private final FeedSubscriptionDAO feedSubscriptionDAO;
	private final FeedEntryDAO feedEntryDAO;
	private final FeedEntryStatusDAO feedEntryStatusDAO;
	private final FeedEntryContentService feedEntryContentService;
	private final FeedEntryFilteringService feedEntryFilteringService;

	public FeedEntry find(Feed feed, Entry entry) {
		String guidHash = Digests.sha1Hex(entry.guid());
		return feedEntryDAO.findExisting(guidHash, feed);
	}

	public FeedEntry create(Feed feed, Entry entry) {
		FeedEntry feedEntry = new FeedEntry();
		feedEntry.setGuid(FeedUtils.truncate(entry.guid(), 2048));
		feedEntry.setGuidHash(Digests.sha1Hex(entry.guid()));
		feedEntry.setUrl(FeedUtils.truncate(entry.url(), 2048));
		feedEntry.setPublished(entry.published());
		feedEntry.setInserted(Instant.now());
		feedEntry.setFeed(feed);
		feedEntry.setContent(feedEntryContentService.findOrCreate(entry.content(), feed.getLink()));

		feedEntryDAO.persist(feedEntry);
		return feedEntry;
	}

	public boolean applyFilter(FeedSubscription sub, FeedEntry entry) {
		boolean matches = true;
		try {
			matches = feedEntryFilteringService.filterMatchesEntry(sub.getFilter(), entry);
		} catch (FeedEntryFilterException e) {
			log.error("could not evaluate filter {}", sub.getFilter(), e);
		}

		/*
		 * Support for the auto-mark-read feature: if the entry matches the filter
		 * (remains unread), but has an auto-mark-as-read limit set,
		 * we must persist a status record even if it is not filtered out. This is
		 * necessary so that the cleanup task can find it later.
		 */
		if (!matches || sub.getAutoMarkAsReadAfterDays() != null) {
			FeedEntryStatus status = new FeedEntryStatus(sub.getUser(), sub, entry);
			status.setRead(!matches);
			feedEntryStatusDAO.persist(status);
		}

		return matches;
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
			feedEntryStatusDAO.merge(status);
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
		feedEntryStatusDAO.merge(status);
	}

	public void markSubscriptionEntries(User user, List<FeedSubscription> subscriptions, Instant olderThan, Instant insertedBefore,
			List<FeedEntryKeyword> keywords) {
		List<FeedEntryStatus> statuses = feedEntryStatusDAO.findBySubscriptions(user, subscriptions, true, keywords, null, -1, -1, null,
				false, null, null, null);
		markList(statuses, olderThan, insertedBefore);
	}

	public void markStarredEntries(User user, Instant olderThan, Instant insertedBefore) {
		List<FeedEntryStatus> statuses = feedEntryStatusDAO.findStarred(user, null, -1, -1, null, false);
		markList(statuses, olderThan, insertedBefore);
	}

	private void markList(List<FeedEntryStatus> statuses, Instant olderThan, Instant insertedBefore) {
		List<FeedEntryStatus> statusesToMark = statuses.stream().filter(FeedEntryStatus::isMarkable).filter(s -> {
			Instant entryDate = s.getEntry().getPublished();
			return olderThan == null || entryDate == null || entryDate.isBefore(olderThan);
		}).filter(s -> {
			Instant insertedDate = s.getEntry().getInserted();
			return insertedBefore == null || insertedDate == null || insertedDate.isBefore(insertedBefore);
		}).toList();

		statusesToMark.forEach(s -> {
			s.setRead(true);
			feedEntryStatusDAO.merge(s);
		});
	}

	public void updateAutoMarkAsReadAfterDays(FeedSubscription sub, Integer days) {
		/*
		 * Support for the auto-mark-read feature: update the subscription's setting and
		 * recalculate or reset the expiration date
		 * for all existing unread entries.
		 */
		if (days != null && days <= 0) {
			days = null;
		}

		sub.setAutoMarkAsReadAfterDays(days);
		feedSubscriptionDAO.merge(sub);

		if (days == null) {
			feedEntryStatusDAO.resetAutoMarkAsReadStatuses(sub);
		} else {
			List<FeedEntryStatus> unreadStatuses = feedEntryStatusDAO.findBySubscriptions(sub.getUser(), List.of(sub), true, null, null, -1,
					-1, null, false, null, null, null);
			Instant now = Instant.now();
			for (FeedEntryStatus status : unreadStatuses) {
				Instant autoMarkAsReadAfter = null;
				if (status.getEntryPublished() != null) {
					autoMarkAsReadAfter = status.getEntryPublished().plusSeconds(days * 24L * 3600L);
				}
				status.setAutoMarkAsReadAfter(autoMarkAsReadAfter);

				/*
				 * Support for the auto-mark-read feature: if the newly calculated date is
				 * already in the past, mark the entry as read
				 * immediately.
				 */
				if (autoMarkAsReadAfter != null && autoMarkAsReadAfter.isBefore(now)) {
					status.setRead(true);
				}
				feedEntryStatusDAO.merge(status);
			}
		}
	}
}
