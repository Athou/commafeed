package com.commafeed.backend.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.feeds.FeedUtils;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.google.common.collect.Lists;

@Singleton
public class FeedUpdateService {

	@Inject
	FeedSubscriptionDAO feedSubscriptionDAO;

	@Inject
	FeedEntryDAO feedEntryDAO;

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	@Lock(LockType.WRITE)
	public void updateEntries(Feed feed, Collection<FeedEntry> entries) {

		List<FeedEntry> existingEntries = getExistingEntries(entries);
		List<FeedSubscription> subscriptions = feedSubscriptionDAO
				.findByFeed(feed);

		List<FeedEntry> entryUpdateList = Lists.newArrayList();
		List<FeedEntryStatus> statusUpdateList = Lists.newArrayList();
		for (FeedEntry entry : entries) {

			FeedEntry foundEntry = findEntry(existingEntries, entry);

			if (foundEntry == null) {
				FeedEntryContent content = entry.getContent();

				content.setContent(FeedUtils.handleContent(content.getContent()));
				String title = FeedUtils.handleContent(content.getTitle());
				if (title != null) {
					content.setTitle(title.substring(0,
							Math.min(2048, title.length())));
				}

				entry.setInserted(Calendar.getInstance().getTime());
				entry.getFeeds().add(feed);
				entryUpdateList.add(entry);
			} else {
				boolean foundFeed = false;
				for (Feed existingFeed : foundEntry.getFeeds()) {
					if (ObjectUtils.equals(existingFeed.getId(), feed.getId())) {
						foundFeed = true;
						break;
					}
				}

				if (!foundFeed) {
					foundEntry.getFeeds().add(feed);
					entryUpdateList.add(foundEntry);
				}
			}
		}
		for (FeedEntry entry : entryUpdateList) {
			for (FeedSubscription sub : subscriptions) {
				FeedEntryStatus status = new FeedEntryStatus();
				status.setEntry(entry);
				status.setSubscription(sub);
				statusUpdateList.add(status);
			}
		}

		feedEntryDAO.saveOrUpdate(entryUpdateList);
		feedEntryStatusDAO.saveOrUpdate(statusUpdateList);
	}

	private FeedEntry findEntry(List<FeedEntry> existingEntries, FeedEntry entry) {
		FeedEntry foundEntry = null;
		for (FeedEntry existingEntry : existingEntries) {
			if (StringUtils.equals(entry.getGuid(), existingEntry.getGuid())
					&& StringUtils.equals(entry.getUrl(),
							existingEntry.getUrl())) {
				foundEntry = existingEntry;
				break;
			}
		}
		return foundEntry;
	}

	private List<FeedEntry> getExistingEntries(Collection<FeedEntry> entries) {
		List<String> guids = Lists.newArrayList();
		for (FeedEntry entry : entries) {
			guids.add(entry.getGuid());
		}
		List<FeedEntry> existingEntries = guids.isEmpty() ? new ArrayList<FeedEntry>()
				: feedEntryDAO.findByGuids(guids);
		return existingEntries;
	}
}
