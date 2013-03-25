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

	private TypedQuery<FeedEntry> unreadQuery(Feed feed, User user) {
		String query = "select e from FeedEntry e where e.feed=:feed and not exists (select s from FeedEntryStatus s where s.entry = e and s.user =:user and s.read = true)";
		TypedQuery<FeedEntry> typedQuery = em.createQuery(query,
				FeedEntry.class);
		typedQuery.setParameter("feed", feed);
		typedQuery.setParameter("user", user);
		return typedQuery;
	}

	public List<FeedEntry> getUnreadEntries(Feed feed, User user) {
		return unreadQuery(feed, user).getResultList();
	}

	public List<FeedEntry> getUnreadEntries(Feed feed, User user, int offset,
			int limit) {
		return unreadQuery(feed, user).setFirstResult(offset)
				.setMaxResults(limit).getResultList();
	}

	private TypedQuery<FeedEntry> allQuery(Feed feed) {
		String query = "select e from FeedEntry e where e.feed=:feed";
		TypedQuery<FeedEntry> typedQuery = em.createQuery(query,
				FeedEntry.class);
		typedQuery.setParameter("feed", feed);
		return typedQuery;
	}

	public List<FeedEntry> getAllEntries(Feed feed) {
		return allQuery(feed).getResultList();
	}

	public List<FeedEntry> getAllEntries(Feed feed, int offset, int limit) {
		return allQuery(feed).setFirstResult(offset).setMaxResults(limit)
				.getResultList();
	}
}
