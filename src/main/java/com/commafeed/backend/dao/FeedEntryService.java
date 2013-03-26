package com.commafeed.backend.dao;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Stateless
@SuppressWarnings("serial")
public class FeedEntryService extends GenericDAO<FeedEntry, Long> {

	@Inject
	FeedService feedService;

	public void updateEntries(String url, Collection<FeedEntry> entries) {
		Feed feed = Iterables.getFirst(
				feedService.findByField(MF.i(MF.p(Feed.class).getUrl()), url),
				null);
		List<String> guids = Lists.newArrayList();
		for (FeedEntry entry : entries) {
			guids.add(entry.getGuid());
		}

		List<FeedEntry> existingEntries = getByGuids(guids);
		for (FeedEntry entry : entries) {
			boolean found = false;
			for (FeedEntry existingEntry : existingEntries) {
				if (StringUtils
						.equals(entry.getGuid(), existingEntry.getGuid())) {
					found = true;
					break;
				}
			}
			if (!found) {
				entry.setFeed(feed);
				save(entry);
			}
		}

		feed.setLastUpdated(Calendar.getInstance().getTime());
		feed.setMessage(null);
		feedService.update(feed);
	}

	public List<FeedEntry> getByGuids(List<String> guids) {
		TypedQuery<FeedEntry> query = em.createNamedQuery("Entry.byGuids",
				FeedEntry.class);
		query.setParameter("guids", guids);
		return query.getResultList();
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

	public List<FeedEntry> getEntries(List<FeedCategory> categories, User user,
			boolean read) {
		return getEntries(categories, user, read, -1, -1);
	}

	public List<FeedEntry> getEntries(List<FeedCategory> categories, User user,
			boolean read, int offset, int limit) {
		String queryName = null;
		if (read) {
			queryName = "Entry.readByCategories";
		} else {
			queryName = "Entry.unreadByCategories";
		}
		TypedQuery<FeedEntry> query = em.createNamedQuery(queryName,
				FeedEntry.class);
		query.setParameter("categories", categories);
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
