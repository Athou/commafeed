package com.commafeed.backend.dao;

import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.uaihebert.model.EasyCriteria;

@Stateless
@SuppressWarnings("serial")
public class FeedEntryStatusService extends GenericDAO<FeedEntryStatus> {

	@Inject
	FeedCategoryService feedCategoryService;

	public FeedEntryStatus findById(User user, Long id) {

		EasyCriteria<FeedEntryStatus> criteria = createCriteria();
		criteria.andEquals(MF.i(proxy().getId()), id);

		criteria.innerJoinFetch(MF.i(proxy().getSubscription()));
		criteria.innerJoinFetch(MF.i(proxy().getEntry()));

		criteria.andJoinEquals(MF.i(proxy().getSubscription()),
				MF.i(MF.p(FeedSubscription.class).getUser()), user);

		FeedEntryStatus status = null;
		try {
			status = criteria.getSingleResult();
		} catch (NoResultException e) {
			status = null;
		}
		return status;
	}

	public List<FeedEntryStatus> getStatusesByKeywords(User user,
			String keywords) {
		return getStatusesByKeywords(user, keywords, -1, -1);
	}

	public List<FeedEntryStatus> getStatusesByKeywords(User user,
			String keywords, int offset, int limit) {
		TypedQuery<FeedEntryStatus> query = em.createNamedQuery(
				"EntryStatus.allByKeywords", FeedEntryStatus.class);
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
		return query.getResultList();
	}

	public List<FeedEntryStatus> getStatuses(User user, boolean unreadOnly) {
		return getStatuses(user, unreadOnly, -1, -1);
	}

	public List<FeedEntryStatus> getStatuses(User user, boolean unreadOnly,
			int offset, int limit) {
		String queryName = unreadOnly ? "EntryStatus.unread"
				: "EntryStatus.all";
		TypedQuery<FeedEntryStatus> query = em.createNamedQuery(queryName,
				FeedEntryStatus.class);
		query.setParameter("user", user);
		if (offset > -1) {
			query.setFirstResult(offset);
		}
		if (limit > -1) {
			query.setMaxResults(limit);
		}
		return query.getResultList();
	}

	/**
	 * Map between subscriptionId and unread count
	 */
	@SuppressWarnings("rawtypes")
	public Map<Long, Long> getUnreadCount(User user) {
		Map<Long, Long> map = Maps.newHashMap();
		Query query = em.createNamedQuery("EntryStatus.unreadCounts");
		query.setParameter("user", user);
		List resultList = query.getResultList();
		for (Object o : resultList) {
			Object[] array = (Object[]) o;
			map.put((Long) array[0], (Long) array[1]);
		}
		return map;
	}

	public List<FeedEntryStatus> getStatuses(Feed feed, User user,
			boolean unreadOnly) {
		return getStatuses(feed, user, unreadOnly, -1, -1);
	}

	public List<FeedEntryStatus> getStatuses(Feed feed, User user,
			boolean unreadOnly, int offset, int limit) {
		String queryName = unreadOnly ? "EntryStatus.unreadByFeed"
				: "EntryStatus.allByFeed";
		TypedQuery<FeedEntryStatus> query = em.createNamedQuery(queryName,
				FeedEntryStatus.class);
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

	public List<FeedEntryStatus> getStatuses(List<FeedCategory> categories,
			User user, boolean unreadOnly) {
		return getStatuses(categories, user, unreadOnly, -1, -1);
	}

	public List<FeedEntryStatus> getStatuses(List<FeedCategory> categories,
			User user, boolean unreadOnly, int offset, int limit) {
		String queryName = unreadOnly ? "EntryStatus.unreadByCategories"
				: "EntryStatus.allByCategories";
		TypedQuery<FeedEntryStatus> query = em.createNamedQuery(queryName,
				FeedEntryStatus.class);
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

	public void markFeedEntries(User user, Feed feed) {
		List<FeedEntryStatus> statuses = getStatuses(feed, user, true);
		update(markList(statuses));
	}

	public void markCategoryEntries(User user, List<FeedCategory> categories) {
		List<FeedEntryStatus> statuses = getStatuses(categories, user, true);
		update(markList(statuses));
	}

	public void markAllEntries(User user) {
		List<FeedEntryStatus> statuses = getStatuses(user, true);
		update(markList(statuses));
	}

	private List<FeedEntryStatus> markList(List<FeedEntryStatus> statuses) {
		List<FeedEntryStatus> list = Lists.newArrayList();
		for (FeedEntryStatus status : statuses) {
			if (!status.isRead()) {
				status.setRead(true);
				list.add(status);
			}
		}
		return list;
	}
}
