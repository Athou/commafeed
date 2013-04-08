package com.commafeed.backend.dao;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.extended.FeedEntryWithStatus;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Stateless
@SuppressWarnings("serial")
public class FeedEntryService extends GenericDAO<FeedEntry> {

	@Inject
	FeedService feedService;

	@Inject
	FeedSubscriptionService feedSubscriptionService;

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
			FeedEntry foundEntry = null;
			for (FeedEntry existingEntry : existingEntries) {
				if (StringUtils
						.equals(entry.getGuid(), existingEntry.getGuid())) {
					foundEntry = existingEntry;
					break;
				}
			}
			if (foundEntry == null) {
				addFeedToEntry(entry, feed);
				save(entry);
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
					update(foundEntry);
				}
			}
		}

		feed.setLastUpdated(Calendar.getInstance().getTime());
		feed.setMessage(null);
		feedService.update(feed);
	}

	private void addFeedToEntry(FeedEntry entry, Feed feed) {
		entry.getFeeds().add(feed);
		List<FeedSubscription> subscriptions = feedSubscriptionService
				.findByFeed(feed);
		for (FeedSubscription sub : subscriptions) {
			FeedEntryStatus status = new FeedEntryStatus();
			status.setEntry(entry);
			status.setUser(sub.getUser());
			em.persist(status);
		}

	}

	public List<FeedEntry> getByGuids(List<String> guids) {
		TypedQuery<FeedEntry> query = em.createNamedQuery("Entry.byGuids",
				FeedEntry.class);
		query.setParameter("guids", guids);
		return query.getResultList();
	}

	public List<FeedEntryWithStatus> getEntriesByKeywords(User user,
			String keywords) {
		return getEntriesByKeywords(user, keywords, -1, -1);
	}

	public List<FeedEntryWithStatus> getEntriesByKeywords(User user,
			String keywords, int offset, int limit) {
		Query query = em.createNamedQuery("Entry.allByKeywords");
		query.setParameter("userId", user.getId());
		query.setParameter("user", user);

		String joinedKeywords = StringUtils.join(
				keywords.toLowerCase().split(" "), "%");
		query.setParameter("keywords", "%" + joinedKeywords + "%");
		if (offset > -1) {
			query.setFirstResult(offset);
		}
		if (limit > -1) {
			query.setMaxResults(limit);
		}
		return buildList(query.getResultList());
	}

	public List<FeedEntryWithStatus> getEntries(User user, boolean unreadOnly) {
		return getEntries(user, unreadOnly, -1, -1);
	}

	public List<FeedEntryWithStatus> getEntries(User user, boolean unreadOnly,
			int offset, int limit) {
		String queryName = null;
		if (unreadOnly) {
			queryName = "Entry.unread";
		} else {
			queryName = "Entry.all";
		}
		Query query = em.createNamedQuery(queryName);
		query.setParameter("userId", user.getId());
		query.setParameter("user", user);
		if (offset > -1) {
			query.setFirstResult(offset);
		}
		if (limit > -1) {
			query.setMaxResults(limit);
		}
		return buildList(query.getResultList());
	}

	public Long getUnreadCount(Feed feed, User user) {
		TypedQuery<Long> query = em.createNamedQuery("Entry.unreadByFeedCount",
				Long.class);
		query.setParameter("feed", feed);
		query.setParameter("userId", user.getId());
		return query.getSingleResult();
	}

	public List<FeedEntryWithStatus> getEntries(Feed feed, User user,
			boolean unreadOnly) {
		return getEntries(feed, user, unreadOnly, -1, -1);
	}

	public List<FeedEntryWithStatus> getEntries(Feed feed, User user,
			boolean unreadOnly, int offset, int limit) {
		String queryName = null;
		if (unreadOnly) {
			queryName = "Entry.unreadByFeed";
		} else {
			queryName = "Entry.allByFeed";
		}
		Query query = em.createNamedQuery(queryName);
		query.setParameter("feed", feed);
		query.setParameter("userId", user.getId());
		if (offset > -1) {
			query.setFirstResult(offset);
		}
		if (limit > -1) {
			query.setMaxResults(limit);
		}

		return buildList(query.getResultList());
	}

	public List<FeedEntryWithStatus> getEntries(List<FeedCategory> categories,
			User user, boolean unreadOnly) {
		return getEntries(categories, user, unreadOnly, -1, -1);
	}

	public List<FeedEntryWithStatus> getEntries(List<FeedCategory> categories,
			User user, boolean unreadOnly, int offset, int limit) {
		String queryName = null;
		if (unreadOnly) {
			queryName = "Entry.unreadByCategories";
		} else {
			queryName = "Entry.allByCategories";
		}
		Query query = em.createNamedQuery(queryName);
		query.setParameter("categories", categories);
		query.setParameter("userId", user.getId());
		query.setParameter("user", user);
		if (offset > -1) {
			query.setFirstResult(offset);
		}
		if (limit > -1) {
			query.setMaxResults(limit);
		}
		return buildList(query.getResultList());
	}

	@SuppressWarnings("rawtypes")
	private List<FeedEntryWithStatus> buildList(List list) {
		Set<FeedEntryWithStatus> result = Sets.newLinkedHashSet();
		for (Object object : list) {
			Object[] array = (Object[]) object;
			FeedEntry entry = (FeedEntry) array[0];
			FeedEntryStatus status = (FeedEntryStatus) array[1];
			FeedEntryWithStatus fews = new FeedEntryWithStatus(entry, status);
			result.add(fews);
		}
		return Lists.newArrayList(result);
	}
}
