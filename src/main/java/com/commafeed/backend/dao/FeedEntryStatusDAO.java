package com.commafeed.backend.dao;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.FixedSizeSortedSet;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.Models;
import com.commafeed.backend.model.QFeedEntry;
import com.commafeed.backend.model.QFeedEntryContent;
import com.commafeed.backend.model.QFeedEntryStatus;
import com.commafeed.backend.model.QFeedEntryTag;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.frontend.model.UnreadCount;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mysema.query.jpa.hibernate.HibernateQuery;

public class FeedEntryStatusDAO extends GenericDAO<FeedEntryStatus> {

	private static final String ALIAS_STATUS = "status";
	private static final String ALIAS_ENTRY = "entry";
	private static final String ALIAS_TAG = "tag";

	private FeedEntryDAO feedEntryDAO;
	private FeedEntryTagDAO feedEntryTagDAO;
	private CommaFeedConfiguration config;

	private QFeedEntryStatus status = QFeedEntryStatus.feedEntryStatus;

	public FeedEntryStatusDAO(SessionFactory sessionFactory, FeedEntryDAO feedEntryDAO, FeedEntryTagDAO feedEntryTagDAO,
			CommaFeedConfiguration config) {
		super(sessionFactory);
		this.feedEntryDAO = feedEntryDAO;
		this.feedEntryTagDAO = feedEntryTagDAO;
		this.config = config;
	}

	private static final Comparator<FeedEntryStatus> STATUS_COMPARATOR_DESC = new Comparator<FeedEntryStatus>() {
		@Override
		public int compare(FeedEntryStatus o1, FeedEntryStatus o2) {
			CompareToBuilder builder = new CompareToBuilder();
			builder.append(o2.getEntryUpdated(), o1.getEntryUpdated());
			builder.append(o2.getId(), o1.getId());
			return builder.toComparison();
		};
	};

	private static final Comparator<FeedEntryStatus> STATUS_COMPARATOR_ASC = Ordering.from(STATUS_COMPARATOR_DESC).reverse();

	public FeedEntryStatus getStatus(User user, FeedSubscription sub, FeedEntry entry) {
		List<FeedEntryStatus> statuses = newQuery().from(status).where(status.entry.eq(entry), status.subscription.eq(sub)).list(status);
		FeedEntryStatus status = Iterables.getFirst(statuses, null);
		return handleStatus(user, status, sub, entry);
	}

	private FeedEntryStatus handleStatus(User user, FeedEntryStatus status, FeedSubscription sub, FeedEntry entry) {
		if (status == null) {
			Date unreadThreshold = config.getApplicationSettings().getUnreadThreshold();
			boolean read = unreadThreshold == null ? false : entry.getUpdated().before(unreadThreshold);
			status = new FeedEntryStatus(user, sub, entry);
			status.setRead(read);
			status.setMarkable(!read);
		} else {
			status.setMarkable(true);
		}
		return status;
	}

	private FeedEntryStatus fetchTags(User user, FeedEntryStatus status) {
		List<FeedEntryTag> tags = feedEntryTagDAO.findByEntry(user, status.getEntry());
		status.setTags(tags);
		return status;
	}

	public List<FeedEntryStatus> findStarred(User user, Date newerThan, int offset, int limit, ReadingOrder order, boolean includeContent) {
		HibernateQuery query = newQuery().from(status).where(status.user.eq(user), status.starred.isTrue());
		if (newerThan != null) {
			query.where(status.entryInserted.gt(newerThan));
		}

		if (order == ReadingOrder.asc) {
			query.orderBy(status.entryUpdated.asc(), status.id.asc());
		} else {
			query.orderBy(status.entryUpdated.desc(), status.id.desc());
		}

		query.offset(offset).limit(limit).setTimeout(config.getApplicationSettings().getQueryTimeout());

		List<FeedEntryStatus> statuses = query.list(status);
		for (FeedEntryStatus status : statuses) {
			status = handleStatus(user, status, status.getSubscription(), status.getEntry());
			fetchTags(user, status);
		}
		return lazyLoadContent(includeContent, statuses);
	}

	private Criteria buildSearchCriteria(User user, FeedSubscription sub, boolean unreadOnly, String keywords, Date newerThan, int offset,
			int limit, ReadingOrder order, Date last, String tag) {
		QFeedEntry entry = QFeedEntry.feedEntry;
		QFeedEntryContent content = QFeedEntryContent.feedEntryContent;
		QFeedEntryStatus status = QFeedEntryStatus.feedEntryStatus;
		QFeedEntryTag entryTag = QFeedEntryTag.feedEntryTag;

		Criteria criteria = currentSession().createCriteria(FeedEntry.class, ALIAS_ENTRY);
		criteria.add(Restrictions.eq(entry.feed.getMetadata().getName(), sub.getFeed()));

		if (keywords != null) {
			Criteria contentJoin = criteria.createCriteria(entry.content.getMetadata().getName(), "content", JoinType.INNER_JOIN);

			for (String keyword : StringUtils.split(keywords)) {
				Disjunction or = Restrictions.disjunction();
				or.add(Restrictions.ilike(content.content.getMetadata().getName(), keyword, MatchMode.ANYWHERE));
				or.add(Restrictions.ilike(content.title.getMetadata().getName(), keyword, MatchMode.ANYWHERE));
				contentJoin.add(or);
			}
		}
		Criteria statusJoin = criteria.createCriteria(entry.statuses.getMetadata().getName(), ALIAS_STATUS, JoinType.LEFT_OUTER_JOIN,
				Restrictions.eq(status.subscription.getMetadata().getName(), sub));

		if (unreadOnly && tag == null) {

			Disjunction or = Restrictions.disjunction();
			or.add(Restrictions.isNull(status.read.getMetadata().getName()));
			or.add(Restrictions.eq(status.read.getMetadata().getName(), false));
			statusJoin.add(or);

			Date unreadThreshold = config.getApplicationSettings().getUnreadThreshold();
			if (unreadThreshold != null) {
				criteria.add(Restrictions.ge(entry.updated.getMetadata().getName(), unreadThreshold));
			}
		}

		if (tag != null) {
			Conjunction and = Restrictions.conjunction();
			and.add(Restrictions.eq(entryTag.user.getMetadata().getName(), user));
			and.add(Restrictions.eq(entryTag.name.getMetadata().getName(), tag));
			criteria.createCriteria(entry.tags.getMetadata().getName(), ALIAS_TAG, JoinType.INNER_JOIN, and);
		}

		if (newerThan != null) {
			criteria.add(Restrictions.ge(entry.inserted.getMetadata().getName(), newerThan));
		}

		if (last != null) {
			if (order == ReadingOrder.desc) {
				criteria.add(Restrictions.gt(entry.updated.getMetadata().getName(), last));
			} else {
				criteria.add(Restrictions.lt(entry.updated.getMetadata().getName(), last));
			}
		}

		if (order != null) {
			if (order == ReadingOrder.asc) {
				criteria.addOrder(Order.asc(entry.updated.getMetadata().getName())).addOrder(Order.asc(entry.id.getMetadata().getName()));
			} else {
				criteria.addOrder(Order.desc(entry.updated.getMetadata().getName())).addOrder(Order.desc(entry.id.getMetadata().getName()));
			}
		}
		if (offset > -1) {
			criteria.setFirstResult(offset);
		}
		if (limit > -1) {
			criteria.setMaxResults(limit);
		}
		int timeout = config.getApplicationSettings().getQueryTimeout();
		if (timeout > 0) {
			// hibernate timeout is in seconds, jpa timeout is in millis
			criteria.setTimeout(timeout / 1000);
		}
		return criteria;
	}

	@SuppressWarnings("unchecked")
	public List<FeedEntryStatus> findBySubscriptions(User user, List<FeedSubscription> subs, boolean unreadOnly, String keywords,
			Date newerThan, int offset, int limit, ReadingOrder order, boolean includeContent, boolean onlyIds, String tag) {
		int capacity = offset + limit;
		Comparator<FeedEntryStatus> comparator = order == ReadingOrder.desc ? STATUS_COMPARATOR_DESC : STATUS_COMPARATOR_ASC;
		FixedSizeSortedSet<FeedEntryStatus> set = new FixedSizeSortedSet<FeedEntryStatus>(capacity, comparator);
		for (FeedSubscription sub : subs) {
			Date last = (order != null && set.isFull()) ? set.last().getEntryUpdated() : null;
			Criteria criteria = buildSearchCriteria(user, sub, unreadOnly, keywords, newerThan, -1, capacity, order, last, tag);
			ProjectionList projection = Projections.projectionList();
			projection.add(Projections.property("id"), "id");
			projection.add(Projections.property("updated"), "updated");
			projection.add(Projections.property(ALIAS_STATUS + ".id"), "status_id");
			criteria.setProjection(projection);
			criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			List<Map<String, Object>> list = criteria.list();
			for (Map<String, Object> map : list) {
				Long id = (Long) map.get("id");
				Date updated = (Date) map.get("updated");
				Long statusId = (Long) map.get("status_id");

				FeedEntry entry = new FeedEntry();
				entry.setId(id);
				entry.setUpdated(updated);

				FeedEntryStatus status = new FeedEntryStatus();
				status.setId(statusId);
				status.setEntryUpdated(updated);
				status.setEntry(entry);
				status.setSubscription(sub);

				set.add(status);
			}
		}

		List<FeedEntryStatus> placeholders = set.asList();
		int size = placeholders.size();
		if (size < offset) {
			return Lists.newArrayList();
		}
		placeholders = placeholders.subList(Math.max(offset, 0), size);

		List<FeedEntryStatus> statuses = null;
		if (onlyIds) {
			statuses = placeholders;
		} else {
			statuses = Lists.newArrayList();
			for (FeedEntryStatus placeholder : placeholders) {
				Long statusId = placeholder.getId();
				FeedEntry entry = feedEntryDAO.findById(placeholder.getEntry().getId());
				FeedEntryStatus status = handleStatus(user, statusId == null ? null : findById(statusId), placeholder.getSubscription(),
						entry);
				status = fetchTags(user, status);
				statuses.add(status);
			}
			statuses = lazyLoadContent(includeContent, statuses);
		}
		return statuses;
	}

	@SuppressWarnings("unchecked")
	public UnreadCount getUnreadCount(User user, FeedSubscription subscription) {
		UnreadCount uc = null;
		Criteria criteria = buildSearchCriteria(user, subscription, true, null, null, -1, -1, null, null, null);
		ProjectionList projection = Projections.projectionList();
		projection.add(Projections.rowCount(), "count");
		projection.add(Projections.max(QFeedEntry.feedEntry.updated.getMetadata().getName()), "updated");
		criteria.setProjection(projection);
		criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		List<Map<String, Object>> list = criteria.list();
		for (Map<String, Object> row : list) {
			Long count = (Long) row.get("count");
			Date updated = (Date) row.get("updated");
			uc = new UnreadCount(subscription.getId(), count, updated);
		}
		return uc;
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

	public List<FeedEntryStatus> getOldStatuses(Date olderThan, int limit) {
		return newQuery().from(status).where(status.entryInserted.lt(olderThan), status.starred.isFalse()).limit(limit).list(status);
	}

}
