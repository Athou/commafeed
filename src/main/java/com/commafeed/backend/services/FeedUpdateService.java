package com.commafeed.backend.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.dao.FeedDAO;
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

@Stateless
public class FeedUpdateService {

	@Inject
	FeedDAO feedDAO;

	@Inject
	FeedSubscriptionDAO feedSubscriptionDAO;

	@Inject
	FeedEntryDAO feedEntryDAO;

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	public void updateEntries(Feed feed, Collection<FeedEntry> entries) {
		List<String> guids = Lists.newArrayList();
		for (FeedEntry entry : entries) {
			guids.add(entry.getGuid());
		}

		List<FeedEntry> existingEntries = guids.isEmpty() ? new ArrayList<FeedEntry>()
				: feedEntryDAO.findByGuids(guids);
		for (FeedEntry entry : entries) {
			FeedEntry foundEntry = null;
			for (FeedEntry existingEntry : existingEntries) {
				if (StringUtils
						.equals(entry.getGuid(), existingEntry.getGuid())) {
					foundEntry = existingEntry;
					break;
				}
			}
			if (foundEntry == null) {
				FeedEntryContent content = entry.getContent();
				content.setContent(FeedUtils.handleContent(content.getContent()));

				String title = FeedUtils.handleContent(content.getTitle());
				if (title != null) {
					content.setTitle(title.substring(0,
							Math.min(2048, title.length())));
				}

				entry.setInserted(Calendar.getInstance().getTime());
				addFeedToEntry(entry, feed);
			} else {
				boolean foundFeed = false;
				for (Feed existingFeed : foundEntry.getFeeds()) {
					if (ObjectUtils.equals(existingFeed.getId(), feed.getId())) {
						foundFeed = true;
						break;
					}
				}

				if (!foundFeed) {
					addFeedToEntry(foundEntry, feed);
				}
			}
		}
	}

	private void addFeedToEntry(FeedEntry entry, Feed feed) {
		entry.getFeeds().add(feed);
		feedEntryDAO.saveOrUpdate(entry);
		List<FeedSubscription> subscriptions = feedSubscriptionDAO
				.findByFeed(feed);
		for (FeedSubscription sub : subscriptions) {
			FeedEntryStatus status = new FeedEntryStatus();
			status.setEntry(entry);
			status.setSubscription(sub);
			feedEntryStatusDAO.save(status);
		}

	}

}
