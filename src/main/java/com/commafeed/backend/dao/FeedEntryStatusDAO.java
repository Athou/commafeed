package com.commafeed.backend.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntryContent_;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedEntryStatus_;
import com.commafeed.backend.model.FeedEntry_;
import com.commafeed.backend.model.FeedSubscription_;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.uaihebert.model.EasyCriteria;

@Stateless
public class FeedEntryStatusDAO extends GenericDAO<FeedEntryStatus> {

	public FeedEntryStatus findById(User user, Long id) {

		EasyCriteria<FeedEntryStatus> criteria = createCriteria();
		criteria.andEquals(FeedEntryStatus_.id.getName(), id);

		criteria.innerJoinFetch(FeedEntryStatus_.subscription.getName());
		criteria.innerJoinFetch(FeedEntryStatus_.entry.getName());

		criteria.andJoinEquals(FeedEntryStatus_.subscription.getName(),
				FeedSubscription_.user.getName(), user);

		FeedEntryStatus status = null;
		try {
			status = criteria.getSingleResult();
		} catch (NoResultException e) {
			status = null;
		}
		return status;
	}

	public List<FeedEntryStatus> findByKeywords(User user, String keywords,
			int offset, int limit, boolean includeContent) {

		String joinedKeywords = StringUtils.join(
				keywords.toLowerCase().split(" "), "%");
		joinedKeywords = "%" + joinedKeywords + "%";

		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		List<Predicate> predicates = Lists.newArrayList();
		predicates.add(builder.equal(root.get(FeedEntryStatus_.subscription)
				.get(FeedSubscription_.user), user));

		Predicate content = builder.like(builder.lower(root
				.get(FeedEntryStatus_.entry).get(FeedEntry_.content)
				.get(FeedEntryContent_.content)), joinedKeywords);
		Predicate title = builder.like(
				builder.lower(root.get(FeedEntryStatus_.entry)
						.get(FeedEntry_.content).get(FeedEntryContent_.title)),
				joinedKeywords);
		predicates.add(builder.or(content, title));
		if (includeContent) {
			root.fetch(FeedEntryStatus_.entry).fetch(FeedEntry_.content);
		}

		query.where(predicates.toArray(new Predicate[0]));

		orderBy(query, root, ReadingOrder.desc);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		return q.getResultList();
	}

	public List<FeedEntryStatus> findAll(User user, boolean unreadOnly,
			ReadingOrder order, boolean includeContent) {
		return findAll(user, unreadOnly, -1, -1, order, includeContent);
	}

	public List<FeedEntryStatus> findAll(User user, boolean unreadOnly,
			int offset, int limit, ReadingOrder order, boolean includeContent) {
		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		List<Predicate> predicates = Lists.newArrayList();
		predicates.add(builder.equal(root.get(FeedEntryStatus_.subscription)
				.get(FeedSubscription_.user), user));
		if (unreadOnly) {
			predicates.add(builder.isFalse(root.get(FeedEntryStatus_.read)));
		}

		if (includeContent) {
			root.fetch(FeedEntryStatus_.entry).fetch(FeedEntry_.content);
		}

		query.where(predicates.toArray(new Predicate[0]));
		orderBy(query, root, order);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		return q.getResultList();
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

	public List<FeedEntryStatus> findByFeed(Feed feed, User user,
			boolean unreadOnly, ReadingOrder order, boolean includeContent) {
		return findByFeed(feed, user, unreadOnly, -1, -1, order, includeContent);
	}

	public List<FeedEntryStatus> findByFeed(Feed feed, User user,
			boolean unreadOnly, int offset, int limit, ReadingOrder order,
			boolean includeContent) {

		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		List<Predicate> predicates = Lists.newArrayList();
		predicates.add(builder.equal(root.get(FeedEntryStatus_.subscription)
				.get(FeedSubscription_.user), user));
		predicates.add(builder.equal(root.get(FeedEntryStatus_.subscription)
				.get(FeedSubscription_.feed), feed));
		if (unreadOnly) {
			predicates.add(builder.isFalse(root.get(FeedEntryStatus_.read)));
		}

		if (includeContent) {
			root.fetch(FeedEntryStatus_.entry).fetch(FeedEntry_.content);
		}

		query.where(predicates.toArray(new Predicate[0]));

		orderBy(query, root, order);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		return q.getResultList();
	}

	public List<FeedEntryStatus> findByCategories(
			List<FeedCategory> categories, User user, boolean unreadOnly,
			ReadingOrder order, boolean includeContent) {
		return findByCategories(categories, user, unreadOnly, -1, -1, order,
				includeContent);
	}

	public List<FeedEntryStatus> findByCategories(
			List<FeedCategory> categories, User user, boolean unreadOnly,
			int offset, int limit, ReadingOrder order, boolean includeContent) {

		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		List<Predicate> predicates = Lists.newArrayList();
		predicates.add(builder.equal(root.get(FeedEntryStatus_.subscription)
				.get(FeedSubscription_.user), user));
		predicates.add(root.get(FeedEntryStatus_.subscription)
				.get(FeedSubscription_.category).in(categories));
		if (unreadOnly) {
			predicates.add(builder.isFalse(root.get(FeedEntryStatus_.read)));
		}

		if (includeContent) {
			root.fetch(FeedEntryStatus_.entry).fetch(FeedEntry_.content);
		}

		query.where(predicates.toArray(new Predicate[0]));

		orderBy(query, root, order);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		return q.getResultList();
	}

	private void orderBy(CriteriaQuery<FeedEntryStatus> query,
			Root<FeedEntryStatus> root, ReadingOrder order) {
		Path<Date> orderPath = root.get(FeedEntryStatus_.entry).get(
				FeedEntry_.updated);
		if (order == ReadingOrder.asc) {
			query.orderBy(builder.asc(orderPath));
		} else {
			query.orderBy(builder.desc(orderPath));
		}

	}

	public void markFeedEntries(User user, Feed feed, Date olderThan) {
		List<FeedEntryStatus> statuses = findByFeed(feed, user, true,
				ReadingOrder.desc, false);
		update(markList(statuses, olderThan));
	}

	public void markCategoryEntries(User user, List<FeedCategory> categories,
			Date olderThan) {
		List<FeedEntryStatus> statuses = findByCategories(categories, user,
				true, ReadingOrder.desc, false);
		update(markList(statuses, olderThan));
	}

	public void markAllEntries(User user, Date olderThan) {
		List<FeedEntryStatus> statuses = findAll(user, true, ReadingOrder.desc,
				false);
		update(markList(statuses, olderThan));
	}

	private List<FeedEntryStatus> markList(List<FeedEntryStatus> statuses,
			Date olderThan) {
		List<FeedEntryStatus> list = Lists.newArrayList();
		for (FeedEntryStatus status : statuses) {
			if (!status.isRead()) {
				Date inserted = status.getEntry().getInserted();
				if (olderThan == null || inserted == null
						|| olderThan.after(inserted)) {
					status.setRead(true);
					list.add(status);
				}
			}
		}
		return list;
	}
}
