package com.commafeed.backend.dao;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;

import com.commafeed.model.Feed;
import com.commafeed.model.FeedEntry;
import com.commafeed.model.User;

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

	public List<FeedEntry> getUnreadEntries(Feed feed, User user) {
		String query = "select e from FeedEntry e where e.feed=:feed and not exists (select s from FeedEntryStatus s where s.entry = e and s.user =:user and s.read = true)";
		TypedQuery<FeedEntry> typedQuery = em.createQuery(query,
				FeedEntry.class);
		typedQuery.setParameter("feed", feed);
		typedQuery.setParameter("user", user);
		return typedQuery.getResultList();
	}

}
