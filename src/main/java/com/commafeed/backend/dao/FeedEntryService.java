package com.commafeed.backend.dao;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.common.collect.Iterables;

@Stateless
@SuppressWarnings("serial")
public class FeedEntryService extends GenericDAO<FeedEntry, Long> {

	@Inject
	FeedService feedService;

	public void updateEntries(String url, Collection<FeedEntry> entries) {
		Feed feed = Iterables.getFirst(
				feedService.findByField(MF.i(MF.p(Feed.class).getUrl()), url),
				null);
		for (FeedEntry entry : entries) {
			FeedEntry existing = Iterables.getFirst(
					findByField(MF.i(MF.p(getType()).getGuid()),
							entry.getGuid()), null);
			if (existing == null) {
				entry.setFeed(feed);
				save(entry);
			}
		}
		feed.setLastUpdated(Calendar.getInstance().getTime());
		feed.setMessage(null);
		em.merge(feed);
	}

	public List<FeedEntry> getEntries(Feed feed, User user, boolean read) {
		return getEntries(feed, user, read, -1, -1);
	}

	public List<FeedEntry> getEntries(Feed feed, User user, boolean read,
			int offset, int limit) {
		String queryName = null;
		if (read) {
			queryName = "Entry.readByFeed";
		} else {
			queryName = "Entry.unreadByFeed";
		}
		TypedQuery<FeedEntry> query = em.createNamedQuery(queryName,
				FeedEntry.class);
		query.setParameter("feed", feed);
		query.setParameter("user", user);
		if (offset > -1) {
			query.setFirstResult(offset);
		}
		if (limit > -1) {
			query.setMaxResults(limit);
		}
		return query.getResultList();
	}
}
