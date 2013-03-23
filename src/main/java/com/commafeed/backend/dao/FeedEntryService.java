package com.commafeed.backend.dao;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;

import com.commafeed.frontend.utils.ModelFactory.MF;
import com.commafeed.model.Feed;
import com.commafeed.model.FeedEntry;
import com.commafeed.model.User;
import com.google.common.collect.Iterables;

@Stateless
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
