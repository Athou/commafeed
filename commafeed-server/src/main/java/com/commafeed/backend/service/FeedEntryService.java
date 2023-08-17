package com.commafeed.backend.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;

import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.feed.FeedEntryKeyword;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;

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
	public boolean addEntry(Feed feed, FeedEntry entry, List<FeedSubscription> subscriptions) {

		Long existing = feedEntryDAO.findExisting(entry.getGuid(), feed);
		if (existing != null) {
			return false;
		}

		FeedEntryContent content = feedEntryContentService.findOrCreate(entry.getContent(), feed.getLink());
		entry.setGuidHash(DigestUtils.sha1Hex(entry.getGuid()));
		entry.setContent(content);
		entry.setInserted(new Date());
		entry.setFeed(feed);
		feedEntryDAO.saveOrUpdate(entry);

		// if filter does not match the entry, mark it as read
		for (FeedSubscription sub : subscriptions) {
			boolean matches = true;
			try {
				matches = feedEntryFilteringService.filterMatchesEntry(sub.getFilter(), entry);
			} catch (FeedEntryFilteringService.FeedEntryFilterException e) {
				log.error("could not evaluate filter {}", sub.getFilter(), e);
			}
			if (!matches) {
				FeedEntryStatus status = new FeedEntryStatus(sub.getUser(), sub, entry);
				status.setRead(true);
				feedEntryStatusDAO.saveOrUpdate(status);
			}
		}

		return true;
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

	public void markSubscriptionEntries(User user, List<FeedSubscription> subscriptions, Date olderThan, List<FeedEntryKeyword> keywords) {
		List<FeedEntryStatus> statuses = feedEntryStatusDAO.findBySubscriptions(user, subscriptions, true, keywords, null, -1, -1, null,
				false, false, null, null, null);
		markList(statuses, olderThan);
		cache.invalidateUnreadCount(subscriptions.toArray(new FeedSubscription[0]));
		cache.invalidateUserRootCategory(user);
	}

	public void markStarredEntries(User user, Date olderThan) {
		List<FeedEntryStatus> statuses = feedEntryStatusDAO.findStarred(user, null, -1, -1, null, false);
		markList(statuses, olderThan);
	}

	private void markList(List<FeedEntryStatus> statuses, Date olderThan) {
		List<FeedEntryStatus> list = new ArrayList<>();
		for (FeedEntryStatus status : statuses) {
			if (!status.isRead()) {
				Date entryDate = status.getEntry().getUpdated();
				if (olderThan == null || entryDate == null || olderThan.after(entryDate)) {
					status.setRead(true);
					list.add(status);
				}
			}
		}
		feedEntryStatusDAO.saveOrUpdate(list);
	}
}
