package com.commafeed.backend.dao;

import java.util.Calendar;
import java.util.Collection;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.commafeed.model.Feed;
import com.commafeed.model.FeedEntry;

@Stateless
public class FeedEntryService extends GenericDAO<FeedEntry, String> {

	@Inject
	FeedService feedService;

	public void updateEntries(String url, Collection<FeedEntry> entries) {
		Feed feed = feedService.findById(url);
		for (FeedEntry entry : entries) {
			FeedEntry existing = findById(entry.getGuid());
			if (existing == null) {
				entry.setFeed(feed);
				save(entry);
			}
		}
		feed.setLastUpdated(Calendar.getInstance().getTime());
		em.merge(feed);
	}

}
