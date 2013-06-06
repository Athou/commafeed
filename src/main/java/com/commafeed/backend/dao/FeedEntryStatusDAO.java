package com.commafeed.backend.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryContent_;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedEntryStatus_;
import com.commafeed.backend.model.FeedEntry_;
import com.commafeed.backend.model.FeedSubscription;
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
			int offset, int limit) {

		String joinedKeywords = StringUtils.join(
				keywords.toLowerCase().split(" "), "%");
		joinedKeywords = "%" + joinedKeywords + "%";

		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		List<Predicate> predicates = Lists.newArrayList();

		Join<FeedEntryStatus, FeedEntry> entryJoin = root.join(
				FeedEntryStatus_.entry, JoinType.LEFT);
		Join<FeedEntryStatus, FeedSubscription> subJoin = root.join(
				FeedEntryStatus_.subscription, JoinType.LEFT);

		Join<FeedEntry, FeedEntryContent> contentJoin = entryJoin.join(
				FeedEntry_.content, JoinType.LEFT);

		predicates
				.add(builder.equal(subJoin.get(FeedSubscription_.user), user));

		Predicate content = builder.like(
				builder.lower(contentJoin.get(FeedEntryContent_.content)),
				joinedKeywords);
		Predicate title = builder.like(
				builder.lower(contentJoin.get(FeedEntryContent_.title)),
				joinedKeywords);
		predicates.add(builder.or(content, title));

		query.where(predicates.toArray(new Predicate[0]));

		orderBy(query, entryJoin, ReadingOrder.desc);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		return lazyLoadContent(true, q.getResultList());
	}

	public List<FeedEntryStatus> findStarred(User user, ReadingOrder order,
			boolean includeContent) {
		return findStarred(user, -1, -1, order, includeContent);
	}

	public List<FeedEntryStatus> findStarred(User user, int offset, int limit,
			ReadingOrder order, boolean includeContent) {

		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		List<Predicate> predicates = Lists.newArrayList();

		Join<FeedEntryStatus, FeedEntry> entryJoin = root.join(
				FeedEntryStatus_.entry, JoinType.LEFT);

		Join<FeedEntryStatus, FeedSubscription> subJoin = root.join(
				FeedEntryStatus_.subscription, JoinType.LEFT);

		predicates
				.add(builder.equal(subJoin.get(FeedSubscription_.user), user));
		predicates.add(builder.equal(root.get(FeedEntryStatus_.starred), true));
		query.where(predicates.toArray(new Predicate[0]));

		orderBy(query, entryJoin, order);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		return lazyLoadContent(includeContent, q.getResultList());
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

		Join<FeedEntryStatus, FeedEntry> entryJoin = root.join(
				FeedEntryStatus_.entry, JoinType.LEFT);
		Join<FeedEntryStatus, FeedSubscription> subJoin = root.join(
				FeedEntryStatus_.subscription, JoinType.LEFT);

		predicates
				.add(builder.equal(subJoin.get(FeedSubscription_.user), user));
		if (unreadOnly) {
			predicates.add(builder.isFalse(root.get(FeedEntryStatus_.read)));
		}

		query.where(predicates.toArray(new Predicate[0]));
		orderBy(query, entryJoin, order);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		return lazyLoadContent(includeContent, q.getResultList());
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

		Join<FeedEntryStatus, FeedEntry> entryJoin = root.join(
				FeedEntryStatus_.entry, JoinType.LEFT);
		Join<FeedEntryStatus, FeedSubscription> subJoin = root.join(
				FeedEntryStatus_.subscription, JoinType.LEFT);

		predicates
				.add(builder.equal(subJoin.get(FeedSubscription_.user), user));
		predicates
				.add(builder.equal(subJoin.get(FeedSubscription_.feed), feed));
		if (unreadOnly) {
			predicates.add(builder.isFalse(root.get(FeedEntryStatus_.read)));
		}

		query.where(predicates.toArray(new Predicate[0]));

		orderBy(query, entryJoin, order);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		return lazyLoadContent(includeContent, q.getResultList());
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

		Join<FeedEntryStatus, FeedEntry> entryJoin = root.join(
				FeedEntryStatus_.entry, JoinType.LEFT);
		Join<FeedEntryStatus, FeedSubscription> subJoin = root.join(
				FeedEntryStatus_.subscription, JoinType.LEFT);

		predicates
				.add(builder.equal(subJoin.get(FeedSubscription_.user), user));
		predicates.add(subJoin.get(FeedSubscription_.category).in(categories));
		if (unreadOnly) {
			predicates.add(builder.isFalse(root.get(FeedEntryStatus_.read)));
		}

		query.where(predicates.toArray(new Predicate[0]));

		orderBy(query, entryJoin, order);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		return lazyLoadContent(includeContent, q.getResultList());
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

	private List<FeedEntryStatus> lazyLoadContent(boolean includeContent,
			List<FeedEntryStatus> results) {
		if (includeContent) {
			for (FeedEntryStatus status : results) {
				status.getSubscription().getFeed().getUrl();
				status.getEntry().getContent().getContent();
			}
		}
		return results;
	}

	private void orderBy(CriteriaQuery<FeedEntryStatus> query,
			Join<FeedEntryStatus, FeedEntry> entryJoin, ReadingOrder order) {
		Path<Date> orderPath = entryJoin.get(FeedEntry_.updated);
		if (order == ReadingOrder.asc) {
			query.orderBy(builder.asc(orderPath));
		} else {
			query.orderBy(builder.desc(orderPath));
		}
	}

	public void markFeedEntries(User user, Feed feed, Date olderThan) {
		List<FeedEntryStatus> statuses = findByFeed(feed, user, true,
				ReadingOrder.desc, false);
		saveOrUpdate(markList(statuses, olderThan));
	}

	public void markCategoryEntries(User user, List<FeedCategory> categories,
			Date olderThan) {
		List<FeedEntryStatus> statuses = findByCategories(categories, user,
				true, ReadingOrder.desc, false);
		saveOrUpdate(markList(statuses, olderThan));
	}

	public void markStarredEntries(User user, Date olderThan) {
		List<FeedEntryStatus> statuses = findStarred(user, ReadingOrder.desc,
				false);
		saveOrUpdate(markList(statuses, olderThan));
	}

	public void markAllEntries(User user, Date olderThan) {
		List<FeedEntryStatus> statuses = findAll(user, true, ReadingOrder.desc,
				false);
		saveOrUpdate(markList(statuses, olderThan));
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
