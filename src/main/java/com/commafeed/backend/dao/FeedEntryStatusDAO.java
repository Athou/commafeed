package com.commafeed.backend.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.commafeed.backend.model.Feed_;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Stateless
public class FeedEntryStatusDAO extends GenericDAO<FeedEntryStatus> {

	protected static Logger log = LoggerFactory
			.getLogger(FeedEntryStatusDAO.class);

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@SuppressWarnings("unchecked")
	public FeedEntryStatus findById(User user, Long id) {

		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		Join<FeedEntryStatus, FeedSubscription> join = (Join<FeedEntryStatus, FeedSubscription>) root
				.fetch(FeedEntryStatus_.subscription);

		Predicate p1 = builder.equal(root.get(FeedEntryStatus_.id), id);
		Predicate p2 = builder.equal(join.get(FeedSubscription_.user), user);

		query.where(p1, p2);

		FeedEntryStatus status = null;
		try {
			status = em.createQuery(query).getSingleResult();
		} catch (NoResultException e) {
			status = null;
		}
		return status;
	}

	public FeedEntryStatus findByEntry(FeedEntry entry, FeedSubscription sub) {

		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		Predicate p1 = builder.equal(root.get(FeedEntryStatus_.entry), entry);
		Predicate p2 = builder.equal(root.get(FeedEntryStatus_.subscription),
				sub);

		query.where(p1, p2);

		List<FeedEntryStatus> statuses = em.createQuery(query).getResultList();
		return Iterables.getFirst(statuses, null);
	}

	public List<FeedEntryStatus> findByEntries(List<FeedEntry> entries,
			FeedSubscription sub) {
		List<FeedEntryStatus> results = Lists.newArrayList();

		if (CollectionUtils.isEmpty(entries)) {
			return results;
		}

		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		Predicate p1 = root.get(FeedEntryStatus_.entry).in(entries);
		Predicate p2 = builder.equal(root.get(FeedEntryStatus_.subscription),
				sub);

		query.where(p1, p2);

		Map<Long, FeedEntryStatus> existing = Maps.uniqueIndex(
				em.createQuery(query).getResultList(),
				new Function<FeedEntryStatus, Long>() {
					@Override
					public Long apply(FeedEntryStatus input) {
						return input.getEntry().getId();
					}
				});

		for (FeedEntry entry : entries) {
			FeedEntryStatus s = existing.get(entry.getId());
			if (s == null) {
				s = new FeedEntryStatus();
				s.setEntry(entry);
				s.setSubscription(sub);
				s.setRead(true);
			}
			results.add(s);
		}
		return results;
	}

	public List<FeedEntryStatus> findByKeywords(User user, String keywords,
			int offset, int limit) {

		String joinedKeywords = StringUtils.join(
				keywords.toLowerCase().split(" "), "%");
		joinedKeywords = "%" + joinedKeywords + "%";

		CriteriaQuery<Tuple> query = builder.createTupleQuery();
		Root<FeedEntry> root = query.from(FeedEntry.class);

		SetJoin<FeedEntry, Feed> feedJoin = root.join(FeedEntry_.feeds);
		SetJoin<Feed, FeedSubscription> subJoin = feedJoin
				.join(Feed_.subscriptions);
		Join<FeedEntry, FeedEntryContent> contentJoin = root
				.join(FeedEntry_.content);

		Selection<FeedEntry> entryAlias = root.alias("entry");
		Selection<FeedSubscription> subAlias = subJoin.alias("subscription");
		query.multiselect(entryAlias, subAlias);

		List<Predicate> predicates = Lists.newArrayList();

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
		orderBy(query, root, ReadingOrder.desc);

		TypedQuery<Tuple> q = em.createQuery(query);
		limit(q, offset, limit);
		setTimeout(q);

		List<Tuple> list = q.getResultList();
		List<FeedEntryStatus> results = Lists.newArrayList();
		for (Tuple tuple : list) {
			FeedEntry entry = tuple.get(entryAlias);
			FeedSubscription subscription = tuple.get(subAlias);

			FeedEntryStatus status = findByEntry(entry, subscription);
			if (status == null) {
				status = new FeedEntryStatus();
				status.setEntry(entry);
				status.setRead(true);
				status.setSubscription(subscription);
			}
			results.add(status);
		}

		return lazyLoadContent(true, results);
	}

	public List<FeedEntryStatus> findStarred(User user, ReadingOrder order,
			boolean includeContent) {
		return findStarred(user, null, -1, -1, order, includeContent);
	}

	public List<FeedEntryStatus> findStarred(User user, Date newerThan,
			int offset, int limit, ReadingOrder order, boolean includeContent) {

		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		List<Predicate> predicates = Lists.newArrayList();

		Join<FeedEntryStatus, FeedEntry> entryJoin = root
				.join(FeedEntryStatus_.entry);

		Join<FeedEntryStatus, FeedSubscription> subJoin = root
				.join(FeedEntryStatus_.subscription);

		predicates
				.add(builder.equal(subJoin.get(FeedSubscription_.user), user));
		predicates.add(builder.equal(root.get(FeedEntryStatus_.starred), true));
		query.where(predicates.toArray(new Predicate[0]));

		if (newerThan != null) {
			predicates.add(builder.greaterThanOrEqualTo(
					entryJoin.get(FeedEntry_.inserted), newerThan));
		}

		orderBy(query, entryJoin, order);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		setTimeout(q);
		return lazyLoadContent(includeContent, q.getResultList());
	}

	public List<FeedEntryStatus> findAll(User user, Date newerThan, int offset,
			int limit, ReadingOrder order, boolean includeContent) {

		CriteriaQuery<Tuple> query = builder.createTupleQuery();
		Root<FeedEntry> root = query.from(FeedEntry.class);

		SetJoin<FeedEntry, Feed> feedJoin = root.join(FeedEntry_.feeds);
		SetJoin<Feed, FeedSubscription> subJoin = feedJoin
				.join(Feed_.subscriptions);

		Selection<FeedEntry> entryAlias = root.alias("entry");
		Selection<FeedSubscription> subAlias = subJoin.alias("subscription");
		query.multiselect(entryAlias, subAlias);

		List<Predicate> predicates = Lists.newArrayList();

		predicates
				.add(builder.equal(subJoin.get(FeedSubscription_.user), user));

		if (newerThan != null) {
			predicates.add(builder.greaterThanOrEqualTo(
					root.get(FeedEntry_.inserted), newerThan));
		}

		query.where(predicates.toArray(new Predicate[0]));
		orderBy(query, root, order);

		TypedQuery<Tuple> q = em.createQuery(query);
		limit(q, offset, limit);
		setTimeout(q);

		List<Tuple> list = q.getResultList();
		List<FeedEntryStatus> results = Lists.newArrayList();
		for (Tuple tuple : list) {
			FeedEntry entry = tuple.get(entryAlias);
			FeedSubscription subscription = tuple.get(subAlias);

			FeedEntryStatus status = findByEntry(entry, subscription);
			if (status == null) {
				status = new FeedEntryStatus();
				status.setEntry(entry);
				status.setRead(true);
				status.setSubscription(subscription);
			}
			results.add(status);
		}

		return lazyLoadContent(includeContent, results);
	}

	public List<FeedEntryStatus> findAllUnread(User user, ReadingOrder order,
			boolean includeContent) {
		return findAllUnread(user, null, -1, -1, order, includeContent);
	}

	public List<FeedEntryStatus> findAllUnread(User user, Date newerThan,
			int offset, int limit, ReadingOrder order, boolean includeContent) {
		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		List<Predicate> predicates = Lists.newArrayList();

		Join<FeedEntryStatus, FeedEntry> entryJoin = root
				.join(FeedEntryStatus_.entry);
		Join<FeedEntryStatus, FeedSubscription> subJoin = root
				.join(FeedEntryStatus_.subscription);

		predicates
				.add(builder.equal(subJoin.get(FeedSubscription_.user), user));
		predicates.add(builder.isFalse(root.get(FeedEntryStatus_.read)));

		if (newerThan != null) {
			predicates.add(builder.greaterThanOrEqualTo(
					entryJoin.get(FeedEntry_.inserted), newerThan));
		}

		query.where(predicates.toArray(new Predicate[0]));
		orderBy(query, entryJoin, order);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		setTimeout(q);
		return lazyLoadContent(includeContent, q.getResultList());
	}

	public List<FeedEntryStatus> findBySubscription(
			FeedSubscription subscription, Date newerThan, int offset,
			int limit, ReadingOrder order, boolean includeContent) {

		CriteriaQuery<FeedEntry> query = builder.createQuery(FeedEntry.class);
		Root<FeedEntry> root = query.from(FeedEntry.class);

		SetJoin<FeedEntry, Feed> feedJoin = root.join(FeedEntry_.feeds);
		SetJoin<Feed, FeedSubscription> subJoin = feedJoin
				.join(Feed_.subscriptions);

		List<Predicate> predicates = Lists.newArrayList();

		predicates.add(builder.equal(subJoin.get(FeedSubscription_.id),
				subscription.getId()));

		if (newerThan != null) {
			predicates.add(builder.greaterThanOrEqualTo(
					root.get(FeedEntry_.inserted), newerThan));
		}

		query.where(predicates.toArray(new Predicate[0]));
		orderBy(query, root, order);

		TypedQuery<FeedEntry> q = em.createQuery(query);
		limit(q, offset, limit);
		setTimeout(q);

		List<FeedEntry> list = q.getResultList();
		return lazyLoadContent(includeContent,
				findByEntries(list, subscription));
	}

	public List<FeedEntryStatus> findUnreadBySubscription(
			FeedSubscription subscription, ReadingOrder order,
			boolean includeContent) {
		return findUnreadBySubscription(subscription, null, -1, -1, order,
				includeContent);
	}

	public List<FeedEntryStatus> findUnreadBySubscription(
			FeedSubscription subscription, Date newerThan, int offset,
			int limit, ReadingOrder order, boolean includeContent) {

		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		List<Predicate> predicates = Lists.newArrayList();

		Join<FeedEntryStatus, FeedEntry> entryJoin = root
				.join(FeedEntryStatus_.entry);

		predicates.add(builder.equal(root.get(FeedEntryStatus_.subscription),
				subscription));
		predicates.add(builder.isFalse(root.get(FeedEntryStatus_.read)));

		if (newerThan != null) {
			predicates.add(builder.greaterThanOrEqualTo(
					entryJoin.get(FeedEntry_.inserted), newerThan));
		}

		query.where(predicates.toArray(new Predicate[0]));

		orderBy(query, entryJoin, order);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		setTimeout(q);
		return lazyLoadContent(includeContent, q.getResultList());
	}

	public List<FeedEntryStatus> findByCategories(
			List<FeedCategory> categories, Date newerThan, int offset,
			int limit, ReadingOrder order, boolean includeContent) {

		CriteriaQuery<Tuple> query = builder.createTupleQuery();
		Root<FeedEntry> root = query.from(FeedEntry.class);

		SetJoin<FeedEntry, Feed> feedJoin = root.join(FeedEntry_.feeds);
		SetJoin<Feed, FeedSubscription> subJoin = feedJoin
				.join(Feed_.subscriptions);

		Selection<FeedEntry> entryAlias = root.alias("entry");
		Selection<FeedSubscription> subAlias = subJoin.alias("subscription");
		query.multiselect(entryAlias, subAlias);

		List<Predicate> predicates = Lists.newArrayList();

		if (categories.size() == 1) {
			predicates.add(builder.equal(subJoin
					.get(FeedSubscription_.category), categories.iterator()
					.next()));
		} else {
			predicates.add(subJoin.get(FeedSubscription_.category).in(
					categories));
		}

		if (newerThan != null) {
			predicates.add(builder.greaterThanOrEqualTo(
					root.get(FeedEntry_.inserted), newerThan));
		}

		query.where(predicates.toArray(new Predicate[0]));
		orderBy(query, root, order);

		TypedQuery<Tuple> q = em.createQuery(query);
		limit(q, offset, limit);
		setTimeout(q);

		List<Tuple> list = q.getResultList();
		List<FeedEntryStatus> results = Lists.newArrayList();
		for (Tuple tuple : list) {
			FeedEntry entry = tuple.get(entryAlias);
			FeedSubscription subscription = tuple.get(subAlias);

			FeedEntryStatus status = findByEntry(entry, subscription);
			if (status == null) {
				status = new FeedEntryStatus();
				status.setEntry(entry);
				status.setSubscription(subscription);
				status.setRead(true);
			}
			results.add(status);
		}

		return lazyLoadContent(includeContent, results);

	}

	public List<FeedEntryStatus> findUnreadByCategories(
			List<FeedCategory> categories, ReadingOrder order,
			boolean includeContent) {
		return findUnreadByCategories(categories, null, -1, -1, order,
				includeContent);
	}

	public List<FeedEntryStatus> findUnreadByCategories(
			List<FeedCategory> categories, Date newerThan, int offset,
			int limit, ReadingOrder order, boolean includeContent) {

		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		List<Predicate> predicates = Lists.newArrayList();

		Join<FeedEntryStatus, FeedEntry> entryJoin = root
				.join(FeedEntryStatus_.entry);
		Join<FeedEntryStatus, FeedSubscription> subJoin = root
				.join(FeedEntryStatus_.subscription);

		if (categories.size() == 1) {
			predicates.add(builder.equal(subJoin
					.get(FeedSubscription_.category), categories.iterator()
					.next()));
		} else {
			predicates.add(subJoin.get(FeedSubscription_.category).in(
					categories));
		}

		predicates.add(builder.isFalse(root.get(FeedEntryStatus_.read)));

		if (newerThan != null) {
			predicates.add(builder.greaterThanOrEqualTo(
					entryJoin.get(FeedEntry_.inserted), newerThan));
		}

		query.where(predicates.toArray(new Predicate[0]));

		orderBy(query, entryJoin, order);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		setTimeout(q);
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
		setTimeout(query);
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
				Hibernate.initialize(status.getSubscription().getFeed());
				Hibernate.initialize(status.getEntry().getContent());
			}
		}
		return results;
	}

	private void orderBy(CriteriaQuery<?> query, Path<FeedEntry> entryJoin,
			ReadingOrder order) {
		if (order != null) {
			Path<Date> orderPath = entryJoin.get(FeedEntry_.updated);
			if (order == ReadingOrder.asc) {
				query.orderBy(builder.asc(orderPath));
			} else {
				query.orderBy(builder.desc(orderPath));
			}
		}
	}

	protected void setTimeout(Query query) {
		setTimeout(query, applicationSettingsService.get().getQueryTimeout());
	}

	public void markSubscriptionEntries(FeedSubscription subscription,
			Date olderThan) {
		List<FeedEntryStatus> statuses = findUnreadBySubscription(subscription,
				null, false);
		markList(statuses, olderThan);
	}

	public void markCategoryEntries(User user, List<FeedCategory> categories,
			Date olderThan) {
		List<FeedEntryStatus> statuses = findUnreadByCategories(categories,
				null, false);
		markList(statuses, olderThan);
	}

	public void markStarredEntries(User user, Date olderThan) {
		List<FeedEntryStatus> statuses = findStarred(user, null, false);
		markList(statuses, olderThan);
	}

	public void markAllEntries(User user, Date olderThan) {
		List<FeedEntryStatus> statuses = findAllUnread(user, null, false);
		markList(statuses, olderThan);
	}

	private void markList(List<FeedEntryStatus> statuses, Date olderThan) {
		List<FeedEntryStatus> list = Lists.newArrayList();
		for (FeedEntryStatus status : statuses) {
			if (!status.isRead()) {
				Date inserted = status.getEntry().getInserted();
				if (olderThan == null || inserted == null
						|| olderThan.after(inserted)) {
					if (status.isStarred()) {
						status.setRead(true);
						list.add(status);
					} else {
						delete(status);
					}

				}
			}
		}
		saveOrUpdate(list);
	}

}
