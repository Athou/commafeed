package com.commafeed.backend.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
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
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedEntryStatus_;
import com.commafeed.backend.model.FeedEntry_;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.FeedSubscription_;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
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

		String joinedKeywords = StringUtils.join(
				keywords.toLowerCase().split(" "), "%");
		joinedKeywords = "%" + joinedKeywords + "%";

		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		List<Predicate> predicates = Lists.newArrayList();
		predicates.add(builder.equal(root.get(FeedEntryStatus_.subscription)
				.get(FeedSubscription_.user), user));

		Predicate content = builder.like(
				builder.lower(root.get(FeedEntryStatus_.entry).get(
						FeedEntry_.content)), joinedKeywords);
		Predicate title = builder.like(
				builder.lower(root.get(FeedEntryStatus_.entry).get(
						FeedEntry_.title)), joinedKeywords);
		predicates.add(builder.or(content, title));

		query.where(predicates.toArray(new Predicate[0]));

		orderBy(query, root, ReadingOrder.desc);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		return q.getResultList();
	}

	public List<FeedEntryStatus> getStatuses(User user, boolean unreadOnly,
			ReadingOrder order) {
		return getStatuses(user, unreadOnly, -1, -1, order);
	}

	public List<FeedEntryStatus> getStatuses(User user, boolean unreadOnly,
			int offset, int limit, ReadingOrder order) {
		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		List<Predicate> predicates = Lists.newArrayList();
		predicates.add(builder.equal(root.get(FeedEntryStatus_.subscription)
				.get(FeedSubscription_.user), user));
		if (unreadOnly) {
			predicates.add(builder.isFalse(root.get(FeedEntryStatus_.read)));
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

	public List<FeedEntryStatus> getStatuses(Feed feed, User user,
			boolean unreadOnly, ReadingOrder order) {
		return getStatuses(feed, user, unreadOnly, -1, -1, order);
	}

	public List<FeedEntryStatus> getStatuses(Feed feed, User user,
			boolean unreadOnly, int offset, int limit, ReadingOrder order) {

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
		query.where(predicates.toArray(new Predicate[0]));

		orderBy(query, root, order);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		return q.getResultList();
	}

	public List<FeedEntryStatus> getStatuses(List<FeedCategory> categories,
			User user, boolean unreadOnly, ReadingOrder order) {
		return getStatuses(categories, user, unreadOnly, -1, -1, order);
	}

	public List<FeedEntryStatus> getStatuses(List<FeedCategory> categories,
			User user, boolean unreadOnly, int offset, int limit,
			ReadingOrder order) {

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
		query.where(predicates.toArray(new Predicate[0]));

		orderBy(query, root, order);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		return q.getResultList();
	}

	private void limit(TypedQuery<FeedEntryStatus> query, int offset, int limit) {
		if (offset > -1) {
			query.setFirstResult(offset);
		}
		if (limit > -1) {
			query.setMaxResults(limit);
		}
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
		List<FeedEntryStatus> statuses = getStatuses(feed, user, true,
				ReadingOrder.desc);
		update(markList(statuses, olderThan));
	}

	public void markCategoryEntries(User user, List<FeedCategory> categories,
			Date olderThan) {
		List<FeedEntryStatus> statuses = getStatuses(categories, user, true,
				ReadingOrder.desc);
		update(markList(statuses, olderThan));
	}

	public void markAllEntries(User user, Date olderThan) {
		List<FeedEntryStatus> statuses = getStatuses(user, true,
				ReadingOrder.desc);
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
