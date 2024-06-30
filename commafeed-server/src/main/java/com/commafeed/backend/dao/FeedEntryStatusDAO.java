package com.commafeed.backend.dao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.SessionFactory;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.FixedSizeSortedList;
import com.commafeed.backend.feed.FeedEntryKeyword;
import com.commafeed.backend.feed.FeedEntryKeyword.Mode;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
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
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class FeedEntryStatusDAO extends GenericDAO<FeedEntryStatus> {

	private static final Comparator<FeedEntryStatus> STATUS_COMPARATOR_DESC = (o1, o2) -> {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(o2.getEntryUpdated(), o1.getEntryUpdated());
		builder.append(o2.getId(), o1.getId());
		return builder.toComparison();
	};

	private static final Comparator<FeedEntryStatus> STATUS_COMPARATOR_ASC = Ordering.from(STATUS_COMPARATOR_DESC).reverse();

	private final FeedEntryDAO feedEntryDAO;
	private final FeedEntryTagDAO feedEntryTagDAO;
	private final CommaFeedConfiguration config;

	private final QFeedEntryStatus status = QFeedEntryStatus.feedEntryStatus;
	private final QFeedEntry entry = QFeedEntry.feedEntry;
	private final QFeedEntryContent content = QFeedEntryContent.feedEntryContent;
	private final QFeedEntryTag entryTag = QFeedEntryTag.feedEntryTag;

	@Inject
	public FeedEntryStatusDAO(SessionFactory sessionFactory, FeedEntryDAO feedEntryDAO, FeedEntryTagDAO feedEntryTagDAO,
			CommaFeedConfiguration config) {
		super(sessionFactory);
		this.feedEntryDAO = feedEntryDAO;
		this.feedEntryTagDAO = feedEntryTagDAO;
		this.config = config;
	}

	public FeedEntryStatus getStatus(User user, FeedSubscription sub, FeedEntry entry) {
		List<FeedEntryStatus> statuses = query().selectFrom(status).where(status.entry.eq(entry), status.subscription.eq(sub)).fetch();
		FeedEntryStatus status = Iterables.getFirst(statuses, null);
		return handleStatus(user, status, sub, entry);
	}

	private FeedEntryStatus handleStatus(User user, FeedEntryStatus status, FeedSubscription sub, FeedEntry entry) {
		if (status == null) {
			Instant unreadThreshold = config.getApplicationSettings().getUnreadThreshold();
			boolean read = unreadThreshold != null && entry.getUpdated().isBefore(unreadThreshold);
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

	public List<FeedEntryStatus> findStarred(User user, Instant newerThan, int offset, int limit, ReadingOrder order,
			boolean includeContent) {
		JPAQuery<FeedEntryStatus> query = query().selectFrom(status).where(status.user.eq(user), status.starred.isTrue());
		if (newerThan != null) {
			query.where(status.entryInserted.gt(newerThan));
		}

		if (order == ReadingOrder.asc) {
			query.orderBy(status.entryUpdated.asc(), status.id.asc());
		} else {
			query.orderBy(status.entryUpdated.desc(), status.id.desc());
		}

		if (offset > -1) {
			query.offset(offset);
		}

		if (limit > -1) {
			query.limit(limit);
		}

		setTimeout(query, config.getApplicationSettings().getQueryTimeout());

		List<FeedEntryStatus> statuses = query.fetch();
		for (FeedEntryStatus status : statuses) {
			status = handleStatus(user, status, status.getSubscription(), status.getEntry());
			fetchTags(user, status);
		}
		return lazyLoadContent(includeContent, statuses);
	}

	private JPAQuery<FeedEntry> buildQuery(User user, FeedSubscription sub, boolean unreadOnly, List<FeedEntryKeyword> keywords,
			Instant newerThan, int offset, int limit, ReadingOrder order, FeedEntryStatus last, String tag, Long minEntryId,
			Long maxEntryId) {

		JPAQuery<FeedEntry> query = query().selectFrom(entry).where(entry.feed.eq(sub.getFeed()));

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

			Instant unreadThreshold = config.getApplicationSettings().getUnreadThreshold();
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

		if (minEntryId != null) {
			query.where(entry.id.gt(minEntryId));
		}

		if (maxEntryId != null) {
			query.where(entry.id.lt(maxEntryId));
		}

		if (last != null) {
			if (order == ReadingOrder.desc) {
				query.where(entry.updated.gt(last.getEntryUpdated()));
			} else {
				query.where(entry.updated.lt(last.getEntryUpdated()));
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

		setTimeout(query, config.getApplicationSettings().getQueryTimeout());
		return query;
	}

	public List<FeedEntryStatus> findBySubscriptions(User user, List<FeedSubscription> subs, boolean unreadOnly,
			List<FeedEntryKeyword> keywords, Instant newerThan, int offset, int limit, ReadingOrder order, boolean includeContent,
			boolean onlyIds, String tag, Long minEntryId, Long maxEntryId) {
		int capacity = offset + limit;

		Comparator<FeedEntryStatus> comparator = order == ReadingOrder.desc ? STATUS_COMPARATOR_DESC : STATUS_COMPARATOR_ASC;

		FixedSizeSortedList<FeedEntryStatus> fssl = new FixedSizeSortedList<>(capacity, comparator);
		for (FeedSubscription sub : subs) {
			FeedEntryStatus last = (order != null && fssl.isFull()) ? fssl.last() : null;
			JPAQuery<FeedEntry> query = buildQuery(user, sub, unreadOnly, keywords, newerThan, -1, capacity, order, last, tag, minEntryId,
					maxEntryId);
			List<Tuple> tuples = query.select(entry.id, entry.updated, status.id, entry.content.title).fetch();

			for (Tuple tuple : tuples) {
				Long id = tuple.get(entry.id);
				Instant updated = tuple.get(entry.updated);
				Long statusId = tuple.get(status.id);

				FeedEntryContent content = new FeedEntryContent();
				content.setTitle(tuple.get(entry.content.title));

				FeedEntry entry = new FeedEntry();
				entry.setId(id);
				entry.setUpdated(updated);
				entry.setContent(content);

				FeedEntryStatus status = new FeedEntryStatus();
				status.setId(statusId);
				status.setEntryUpdated(updated);
				status.setEntry(entry);
				status.setSubscription(sub);

				fssl.add(status);
			}
		}

		List<FeedEntryStatus> placeholders = fssl.asList();
		int size = placeholders.size();
		if (size < offset) {
			return new ArrayList<>();
		}
		placeholders = placeholders.subList(Math.max(offset, 0), size);

		List<FeedEntryStatus> statuses;
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
		JPAQuery<FeedEntry> query = buildQuery(user, subscription, true, null, null, -1, -1, null, null, null, null, null);
		List<Tuple> tuples = query.select(entry.count(), entry.updated.max()).fetch();
		for (Tuple tuple : tuples) {
			Long count = tuple.get(entry.count());
			Instant updated = tuple.get(entry.updated.max());
			uc = new UnreadCount(subscription.getId(), count == null ? 0 : count, updated);
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

	public long deleteOldStatuses(Instant olderThan, int limit) {
		List<Long> ids = query().select(status.id)
				.from(status)
				.where(status.entryInserted.lt(olderThan), status.starred.isFalse())
				.limit(limit)
				.fetch();
		return deleteQuery(status).where(status.id.in(ids)).execute();
	}

}
