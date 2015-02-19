package com.commafeed.backend.dao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.SessionFactory;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.FixedSizeSortedSet;
import com.commafeed.backend.feed.FeedEntryKeyword;
import com.commafeed.backend.feed.FeedEntryKeyword.Mode;
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
import com.google.common.collect.Ordering;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.hibernate.HibernateQuery;

@Singleton
public class FeedEntryStatusDAO extends GenericDAO<FeedEntryStatus> {

	private FeedEntryDAO feedEntryDAO;
	private FeedEntryTagDAO feedEntryTagDAO;
	private CommaFeedConfiguration config;

	private QFeedEntryStatus status = QFeedEntryStatus.feedEntryStatus;
	private QFeedEntry entry = QFeedEntry.feedEntry;
	private QFeedEntryContent content = QFeedEntryContent.feedEntryContent;
	private QFeedEntryTag entryTag = QFeedEntryTag.feedEntryTag;

	@Inject
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

		query.offset(offset).limit(limit);
		int timeout = config.getApplicationSettings().getQueryTimeout();
		if (timeout > 0) {
			query.setTimeout(timeout / 1000);
		}

		List<FeedEntryStatus> statuses = query.list(status);
		for (FeedEntryStatus status : statuses) {
			status = handleStatus(user, status, status.getSubscription(), status.getEntry());
			fetchTags(user, status);
		}
		return lazyLoadContent(includeContent, statuses);
	}

	private HibernateQuery buildQuery(User user, FeedSubscription sub, boolean unreadOnly, List<FeedEntryKeyword> keywords, Date newerThan,
			int offset, int limit, ReadingOrder order, Date last, String tag) {

		HibernateQuery query = newQuery().from(entry).where(entry.feed.eq(sub.getFeed()));

		if (CollectionUtils.isNotEmpty(keywords)) {
			query.join(entry.content, content);

			for (FeedEntryKeyword keyword : keywords) {
				BooleanBuilder or = new BooleanBuilder();
				or.or(content.content.containsIgnoreCase(keyword.getKeyword()));
				or.or(content.title.containsIgnoreCase(keyword.getKeyword()));
				if (keyword.getMode() == Mode.EXCLUDE) {
					or.not();
				}
				query.where(or);
			}
		}
		query.leftJoin(entry.statuses, status).on(status.subscription.id.eq(sub.getId()));

		if (unreadOnly && tag == null) {
			BooleanBuilder or = new BooleanBuilder();
			or.or(status.read.isNull());
			or.or(status.read.isFalse());
			query.where(or);

			Date unreadThreshold = config.getApplicationSettings().getUnreadThreshold();
			if (unreadThreshold != null) {
				query.where(entry.updated.goe(unreadThreshold));
			}
		}

		if (tag != null) {
			BooleanBuilder and = new BooleanBuilder();
			and.and(entryTag.user.id.eq(user.getId()));
			and.and(entryTag.name.eq(tag));
			query.join(entry.tags, entryTag).on(and);
		}

		if (newerThan != null) {
			query.where(entry.inserted.goe(newerThan));
		}

		if (last != null) {
			if (order == ReadingOrder.desc) {
				query.where(entry.updated.gt(last));
			} else {
				query.where(entry.updated.lt(last));
			}
		}

		if (order != null) {
			if (order == ReadingOrder.asc) {
				query.orderBy(entry.updated.asc(), entry.id.asc());
			} else {
				query.orderBy(entry.updated.desc(), entry.id.desc());
			}
		}
		if (offset > -1) {
			query.offset(offset);
		}
		if (limit > -1) {
			query.limit(limit);
		}
		int timeout = config.getApplicationSettings().getQueryTimeout();
		if (timeout > 0) {
			query.setTimeout(timeout / 1000);
		}
		return query;
	}

	public List<FeedEntryStatus> findBySubscriptions(User user, List<FeedSubscription> subs, boolean unreadOnly,
			List<FeedEntryKeyword> keywords, Date newerThan, int offset, int limit, ReadingOrder order, boolean includeContent,
			boolean onlyIds, String tag) {
		int capacity = offset + limit;
		Comparator<FeedEntryStatus> comparator = order == ReadingOrder.desc ? STATUS_COMPARATOR_DESC : STATUS_COMPARATOR_ASC;
		FixedSizeSortedSet<FeedEntryStatus> set = new FixedSizeSortedSet<FeedEntryStatus>(capacity, comparator);
		for (FeedSubscription sub : subs) {
			Date last = (order != null && set.isFull()) ? set.last().getEntryUpdated() : null;
			HibernateQuery query = buildQuery(user, sub, unreadOnly, keywords, newerThan, -1, capacity, order, last, tag);
			List<Tuple> tuples = query.list(entry.id, entry.updated, status.id);
			for (Tuple tuple : tuples) {
				Long id = tuple.get(entry.id);
				Date updated = tuple.get(entry.updated);
				Long statusId = tuple.get(status.id);

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
			return new ArrayList<>();
		}
		placeholders = placeholders.subList(Math.max(offset, 0), size);

		List<FeedEntryStatus> statuses = null;
		if (onlyIds) {
			statuses = placeholders;
		} else {
			statuses = new ArrayList<>();
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

	public UnreadCount getUnreadCount(User user, FeedSubscription subscription) {
		UnreadCount uc = null;
		HibernateQuery query = buildQuery(user, subscription, true, null, null, -1, -1, null, null, null);
		List<Tuple> tuples = query.list(entry.count(), entry.updated.max());
		for (Tuple tuple : tuples) {
			Long count = tuple.get(entry.count());
			Date updated = tuple.get(entry.updated.max());
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
