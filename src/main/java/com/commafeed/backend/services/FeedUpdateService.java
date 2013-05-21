package com.commafeed.backend.services;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.MetricsBean;
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
	FeedSubscriptionDAO feedSubscriptionDAO;

	@Inject
	FeedEntryDAO feedEntryDAO;

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	@Inject
	MetricsBean metricsBean;

	public void updateEntry(Feed feed, FeedEntry entry,
			List<FeedSubscription> subscriptions) {

		FeedEntry foundEntry = findEntry(
				feedEntryDAO.findByGuid(entry.getGuid()), entry);

		if (foundEntry == null) {
			handleEntry(feed, entry);
			entry.setInserted(Calendar.getInstance().getTime());
			entry.getFeeds().add(feed);

			foundEntry = entry;
		} else {

			if (!findFeed(foundEntry.getFeeds(), feed)) {
				foundEntry.getFeeds().add(feed);
			}
		}

		if (foundEntry != null) {
			List<FeedEntryStatus> statusUpdateList = Lists.newArrayList();
			for (FeedSubscription sub : subscriptions) {
				FeedEntryStatus status = new FeedEntryStatus();
				status.setEntry(foundEntry);
				status.setSubscription(sub);
				statusUpdateList.add(status);
			}
			feedEntryDAO.saveOrUpdate(foundEntry);
			feedEntryStatusDAO.saveOrUpdate(statusUpdateList);
			metricsBean.entryUpdated(statusUpdateList.size());
		}
	}

	private FeedEntry findEntry(List<FeedEntry> existingEntries, FeedEntry entry) {
		FeedEntry found = null;
		for (FeedEntry existing : existingEntries) {
			if (StringUtils.equals(entry.getGuid(), existing.getGuid())
					&& StringUtils.equals(entry.getUrl(), existing.getUrl())) {
				found = existing;
				break;
			}
		}
		return found;
	}

	private boolean findFeed(Set<Feed> feeds, Feed feed) {
		boolean found = false;
		for (Feed existingFeed : feeds) {
			if (ObjectUtils.equals(existingFeed.getId(), feed.getId())) {
				found = true;
				break;
			}
		}
		return found;
	}

	private void handleEntry(Feed feed, FeedEntry entry) {
		String baseUri = feed.getLink();
		FeedEntryContent content = entry.getContent();

		content.setContent(FeedUtils.handleContent(content.getContent(),
				baseUri));
		String title = FeedUtils.handleContent(content.getTitle(), baseUri);
		if (title != null) {
			content.setTitle(title.substring(0, Math.min(2048, title.length())));
		}
		String author = entry.getAuthor();
		if (author != null) {
			entry.setAuthor(author.substring(0, Math.min(128, author.length())));
		}
	}

}
