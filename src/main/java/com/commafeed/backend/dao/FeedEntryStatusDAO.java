package com.commafeed.backend.dao;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.FixedSizeSortedSet;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent_;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedEntryStatus_;
import com.commafeed.backend.model.FeedEntry_;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.Models;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Stateless
public class FeedEntryStatusDAO extends GenericDAO<FeedEntryStatus> {

	protected static Logger log = LoggerFactory.getLogger(FeedEntryStatusDAO.class);

	private static final String ALIAS_STATUS = "status";
	private static final String ALIAS_ENTRY = "entry";

	private static final Comparator<FeedEntryStatus> STATUS_COMPARATOR_DESC = new Comparator<FeedEntryStatus>() {
		@Override
		public int compare(FeedEntryStatus o1, FeedEntryStatus o2) {
			return ObjectUtils.compare(o2.getEntryUpdated(), o1.getEntryUpdated());
		};
	};

	private static final Comparator<FeedEntryStatus> STATUS_COMPARATOR_ASC = new Comparator<FeedEntryStatus>() {
		@Override
		public int compare(FeedEntryStatus o1, FeedEntryStatus o2) {
			return ObjectUtils.compare(o1.getEntryUpdated(), o2.getEntryUpdated());
		};
	};

	@Inject
	ApplicationSettingsService applicationSettingsService;

	public FeedEntryStatus getStatus(FeedSubscription sub, FeedEntry entry) {

		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		Predicate p1 = builder.equal(root.get(FeedEntryStatus_.entry), entry);
		Predicate p2 = builder.equal(root.get(FeedEntryStatus_.subscription), sub);

		query.where(p1, p2);

		List<FeedEntryStatus> statuses = em.createQuery(query).getResultList();
		FeedEntryStatus status = Iterables.getFirst(statuses, null);

		return handleStatus(status, sub, entry);
	}

	private FeedEntryStatus handleStatus(FeedEntryStatus status, FeedSubscription sub, FeedEntry entry) {
		if (status == null) {
			Date unreadThreshold = applicationSettingsService.getUnreadThreshold();
			boolean read = unreadThreshold == null ? false : entry.getUpdated().before(unreadThreshold);
			status = new FeedEntryStatus(sub.getUser(), sub, entry);
			status.setRead(read);
			status.setMarkable(!read);
		} else {
			status.setMarkable(true);
		}
		return status;
	}

	public List<FeedEntryStatus> findStarred(User user, Date newerThan, int offset, int limit, ReadingOrder order, boolean includeContent) {

		CriteriaQuery<FeedEntryStatus> query = builder.createQuery(getType());
		Root<FeedEntryStatus> root = query.from(getType());

		List<Predicate> predicates = Lists.newArrayList();

		predicates.add(builder.equal(root.get(FeedEntryStatus_.user), user));
		predicates.add(builder.equal(root.get(FeedEntryStatus_.starred), true));
		query.where(predicates.toArray(new Predicate[0]));

		if (newerThan != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(FeedEntryStatus_.entryInserted), newerThan));
		}

		orderStatusesBy(query, root, order);

		TypedQuery<FeedEntryStatus> q = em.createQuery(query);
		limit(q, offset, limit);
		setTimeout(q);
		List<FeedEntryStatus> statuses = q.getResultList();
		for (FeedEntryStatus status : statuses) {
			status = handleStatus(status, status.getSubscription(), status.getEntry());
		}
		return lazyLoadContent(includeContent, statuses);
	}

	private Criteria buildSearchCriteria(FeedSubscription sub, boolean unreadOnly, String keywords, Date newerThan, int offset, int limit,
			ReadingOrder order, boolean includeContent, Date last) {
		Criteria criteria = getSession().createCriteria(FeedEntry.class, ALIAS_ENTRY);

		criteria.add(Restrictions.eq(FeedEntry_.feed.getName(), sub.getFeed()));

		if (keywords != null) {
			Criteria contentJoin = criteria.createCriteria(FeedEntry_.content.getName(), "content", JoinType.INNER_JOIN);

			for (String keyword : keywords.split(" ")) {
				Disjunction or = Restrictions.disjunction();
				or.add(Restrictions.ilike(FeedEntryContent_.content.getName(), keyword, MatchMode.ANYWHERE));
				or.add(Restrictions.ilike(FeedEntryContent_.title.getName(), keyword, MatchMode.ANYWHERE));
				contentJoin.add(or);
			}
		}
		Criteria statusJoin = criteria.createCriteria(FeedEntry_.statuses.getName(), ALIAS_STATUS, JoinType.LEFT_OUTER_JOIN,
				Restrictions.eq(FeedEntryStatus_.subscription.getName(), sub));

		if (unreadOnly) {

			Disjunction or = Restrictions.disjunction();
			or.add(Restrictions.isNull(FeedEntryStatus_.read.getName()));
			or.add(Restrictions.eq(FeedEntryStatus_.read.getName(), false));
			statusJoin.add(or);

			Date unreadThreshold = applicationSettingsService.getUnreadThreshold();
			if (unreadThreshold != null) {
				criteria.add(Restrictions.ge(FeedEntry_.updated.getName(), unreadThreshold));
			}
		}

		if (newerThan != null) {
			criteria.add(Restrictions.ge(FeedEntry_.inserted.getName(), newerThan));
		}

		if (last != null) {
			if (order == ReadingOrder.desc) {
				criteria.add(Restrictions.gt(FeedEntry_.updated.getName(), last));
			} else {
				criteria.add(Restrictions.lt(FeedEntry_.updated.getName(), last));
			}
		}

		if (order != null) {
			Order o = null;
			if (order == ReadingOrder.asc) {
				o = Order.asc(FeedEntry_.updated.getName());
			} else {
				o = Order.desc(FeedEntry_.updated.getName());
			}
			criteria.addOrder(o);
		}
		if (offset > -1) {
			criteria.setFirstResult(offset);
		}
		if (limit > -1) {
			criteria.setMaxResults(limit);
		}
		int timeout = applicationSettingsService.get().getQueryTimeout();
		if (timeout > 0) {
			// hibernate timeout is in seconds, jpa timeout is in millis
			criteria.setTimeout(timeout / 1000);
		}
		return criteria;
	}

	@SuppressWarnings("unchecked")
	public List<FeedEntryStatus> findBySubscriptions(List<FeedSubscription> subscriptions, boolean unreadOnly, String keywords,
			Date newerThan, int offset, int limit, ReadingOrder order, boolean includeContent) {

		int capacity = offset + limit;
		Comparator<FeedEntryStatus> comparator = order == ReadingOrder.desc ? STATUS_COMPARATOR_DESC : STATUS_COMPARATOR_ASC;
		FixedSizeSortedSet<FeedEntryStatus> set = new FixedSizeSortedSet<FeedEntryStatus>(capacity, comparator);
		for (FeedSubscription sub : subscriptions) {
			Date last = (order != null && set.isFull()) ? set.last().getEntryUpdated() : null;
			Criteria criteria = buildSearchCriteria(sub, unreadOnly, keywords, newerThan, -1, capacity, order, includeContent, last);
			ProjectionList projection = Projections.projectionList();
			projection.add(Projections.property("id"), "id");
			projection.add(Projections.property("updated"), "updated");
			criteria.setProjection(projection);
			criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			List<Map<String, Object>> list = criteria.list();
			for (Map<String, Object> map : list) {
				Long id = (Long) map.get("id");
				Date updated = (Date) map.get("updated");

				FeedEntry entry = new FeedEntry();
				entry.setId(id);
				entry.setUpdated(updated);

				FeedEntryStatus status = new FeedEntryStatus();
				status.setEntryUpdated(updated);
				status.setEntry(entry);
				status.setSubscription(sub);

				set.add(status);
			}
		}

		List<FeedEntryStatus> statusPlaceholders = set.asList();
		int size = statusPlaceholders.size();
		if (size < offset) {
			return Lists.newArrayList();
		}

		statusPlaceholders = statusPlaceholders.subList(Math.max(offset, 0), size);

		List<FeedEntryStatus> statuses = Lists.newArrayList();
		for (FeedEntryStatus status : statusPlaceholders) {
			FeedEntry entry = em.find(FeedEntry.class, status.getEntry().getId());
			statuses.add(getStatus(status.getSubscription(), entry));
		}
		return lazyLoadContent(includeContent, statuses);
	}

	@SuppressWarnings("unchecked")
	public Long getUnreadCount(FeedSubscription subscription) {
		Long count = null;
		Criteria criteria = buildSearchCriteria(subscription, true, null, null, -1, -1, null, false, null);
		ProjectionList projection = Projections.projectionList();
		projection.add(Projections.rowCount(), "count");
		criteria.setProjection(projection);
		criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		List<Map<String, Long>> list = criteria.list();
		for (Map<String, Long> row : list) {
			count = row.get("count");
		}
		return count;
	}

	private List<FeedEntryStatus> lazyLoadContent(boolean includeContent, List<FeedEntryStatus> results) {
		if (includeContent) {
			for (FeedEntryStatus status : results) {
				Models.initialize(status.getSubscription().getFeed());
				Models.initialize(status.getEntry().getContent());
			}
		}
		return results;
	}

	private void orderStatusesBy(CriteriaQuery<?> query, Path<FeedEntryStatus> statusJoin, ReadingOrder order) {
		orderBy(query, statusJoin.get(FeedEntryStatus_.entryUpdated), order);
	}

	private void orderBy(CriteriaQuery<?> query, Path<Date> date, ReadingOrder order) {
		if (order != null) {
			if (order == ReadingOrder.asc) {
				query.orderBy(builder.asc(date));
			} else {
				query.orderBy(builder.desc(date));
			}
		}
	}

	protected void setTimeout(Query query) {
		setTimeout(query, applicationSettingsService.get().getQueryTimeout());
	}

	public int deleteOldStatuses(Date olderThan) {
		Query query = em.createNamedQuery("Statuses.deleteOld");
		query.setParameter("date", olderThan);
		return query.executeUpdate();
	}

}
