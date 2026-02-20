package com.commafeed.backend.dao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

import org.apache.commons.collections4.CollectionUtils;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.feed.FeedEntryKeyword;
import com.commafeed.backend.feed.FeedEntryKeyword.Mode;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.QFeed;
import com.commafeed.backend.model.QFeedEntry;
import com.commafeed.backend.model.QFeedEntryContent;
import com.commafeed.backend.model.QFeedEntryStatus;
import com.commafeed.backend.model.QFeedEntryTag;
import com.commafeed.backend.model.QFeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.frontend.model.UnreadCount;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;

@Singleton
public class FeedEntryStatusDAO extends GenericDAO<FeedEntryStatus> {

	private static final QFeedEntryStatus STATUS = QFeedEntryStatus.feedEntryStatus;
	private static final QFeedEntry ENTRY = QFeedEntry.feedEntry;
	private static final QFeed FEED = QFeed.feed;
	private static final QFeedEntryContent CONTENT = QFeedEntryContent.feedEntryContent;
	private static final QFeedEntryTag TAG = QFeedEntryTag.feedEntryTag;
	private static final QFeedSubscription SUBSCRIPTION = QFeedSubscription.feedSubscription;

	private final FeedEntryTagDAO feedEntryTagDAO;
	private final CommaFeedConfiguration config;

	public FeedEntryStatusDAO(EntityManager entityManager, FeedEntryTagDAO feedEntryTagDAO, CommaFeedConfiguration config) {
		super(entityManager, FeedEntryStatus.class);
		this.feedEntryTagDAO = feedEntryTagDAO;
		this.config = config;
	}

	public FeedEntryStatus getStatus(User user, FeedSubscription sub, FeedEntry entry) {
		List<FeedEntryStatus> statuses = query().selectFrom(STATUS).where(STATUS.entry.eq(entry), STATUS.subscription.eq(sub)).fetch();
		FeedEntryStatus status = statuses.stream().findFirst().orElse(null);
		return handleStatus(user, status, sub, entry);
	}

	/**
	 * creates an artificial "unread" status if status is null
	 */
	private FeedEntryStatus handleStatus(User user, FeedEntryStatus status, FeedSubscription sub, FeedEntry entry) {
		if (status == null) {
			Instant statusesInstantThreshold = config.database().cleanup().statusesInstantThreshold();
			boolean read = statusesInstantThreshold != null && entry.getPublished().isBefore(statusesInstantThreshold);
			status = new FeedEntryStatus(user, sub, entry);
			status.setRead(read);
			status.setMarkable(!read);
		} else {
			status.setMarkable(true);
		}
		return status;
	}

	private void fetchTags(User user, List<FeedEntryStatus> statuses) {
		Map<Long, List<FeedEntryTag>> tagsByEntryIds = feedEntryTagDAO.findByEntries(user,
				statuses.stream().map(FeedEntryStatus::getEntry).toList());
		for (FeedEntryStatus status : statuses) {
			List<FeedEntryTag> tags = tagsByEntryIds.get(status.getEntry().getId());
			status.setTags(tags == null ? List.of() : tags);
		}
	}

	public List<FeedEntryStatus> findStarred(User user, Instant newerThan, int offset, int limit, ReadingOrder order,
			boolean includeContent) {
		JPAQuery<FeedEntryStatus> query = query().selectFrom(STATUS).where(STATUS.user.eq(user), STATUS.starred.isTrue());
		if (includeContent) {
			query.join(STATUS.entry).fetchJoin();
			query.join(STATUS.entry.content).fetchJoin();
		}

		if (newerThan != null) {
			query.where(STATUS.entryInserted.gt(newerThan));
		}

		if (order == ReadingOrder.ASC) {
			query.orderBy(STATUS.entryPublished.asc(), STATUS.id.asc());
		} else {
			query.orderBy(STATUS.entryPublished.desc(), STATUS.id.desc());
		}

		if (offset > -1) {
			query.offset(offset);
		}

		if (limit > -1) {
			query.limit(limit);
		}

		setTimeout(query, config.database().queryTimeout());

		List<FeedEntryStatus> statuses = query.fetch();
		statuses.forEach(s -> s.setMarkable(true));
		if (includeContent) {
			fetchTags(user, statuses);
		}

		return statuses;
	}

	public List<FeedEntryStatus> findBySubscriptions(User user, List<FeedSubscription> subs, boolean unreadOnly,
			List<FeedEntryKeyword> keywords, Instant newerThan, int offset, int limit, ReadingOrder order, boolean includeContent,
			String tag, Long minEntryId, Long maxEntryId) {
		Map<Long, List<FeedSubscription>> subsByFeedId = subs.stream().collect(Collectors.groupingBy(s -> s.getFeed().getId()));

		JPAQuery<Tuple> query = query().select(ENTRY, STATUS).from(ENTRY);
		query.leftJoin(ENTRY.statuses, STATUS).on(STATUS.subscription.in(subs));
		query.where(ENTRY.feed.id.in(subsByFeedId.keySet()));

		if (includeContent || CollectionUtils.isNotEmpty(keywords)) {
			query.join(ENTRY.content, CONTENT).fetchJoin();
		}
		if (CollectionUtils.isNotEmpty(keywords)) {
			for (FeedEntryKeyword keyword : keywords) {
				BooleanBuilder or = new BooleanBuilder();
				or.or(CONTENT.content.containsIgnoreCase(keyword.keyword()));
				or.or(CONTENT.title.containsIgnoreCase(keyword.keyword()));
				if (keyword.mode() == Mode.EXCLUDE) {
					or.not();
				}
				query.where(or);
			}
		}

		if (unreadOnly && tag == null) {
			query.where(buildUnreadPredicate());
		}

		if (tag != null) {
			BooleanBuilder and = new BooleanBuilder();
			and.and(TAG.user.id.eq(user.getId()));
			and.and(TAG.name.eq(tag));
			query.join(ENTRY.tags, TAG).on(and);
		}

		if (newerThan != null) {
			query.where(ENTRY.inserted.goe(newerThan));
		}

		if (minEntryId != null) {
			query.where(ENTRY.id.gt(minEntryId));
		}

		if (maxEntryId != null) {
			query.where(ENTRY.id.lt(maxEntryId));
		}

		if (order != null) {
			if (order == ReadingOrder.ASC) {
				query.orderBy(ENTRY.published.asc(), ENTRY.id.asc());
			} else {
				query.orderBy(ENTRY.published.desc(), ENTRY.id.desc());
			}
		}

		if (offset > -1) {
			query.offset(offset);
		}

		if (limit > -1) {
			query.limit(limit);
		}

		setTimeout(query, config.database().queryTimeout());

		List<FeedEntryStatus> statuses = new ArrayList<>();
		List<Tuple> tuples = query.fetch();
		for (Tuple tuple : tuples) {
			FeedEntry e = tuple.get(ENTRY);
			FeedEntryStatus s = tuple.get(STATUS);
			for (FeedSubscription sub : subsByFeedId.get(e.getFeed().getId())) {
				statuses.add(handleStatus(user, s, sub, e));
			}
		}

		if (includeContent) {
			fetchTags(user, statuses);
		}

		return statuses;
	}

	public UnreadCount getUnreadCount(FeedSubscription sub) {
		JPAQuery<Tuple> query = query().select(ENTRY.count(), ENTRY.published.max())
				.from(ENTRY)
				.leftJoin(ENTRY.statuses, STATUS)
				.on(STATUS.subscription.eq(sub))
				.where(ENTRY.feed.eq(sub.getFeed()))
				.where(buildUnreadPredicate());

		Tuple tuple = query.fetchOne();
		Long count = tuple.get(ENTRY.count());
		Instant published = tuple.get(ENTRY.published.max());
		return new UnreadCount(sub.getId(), count == null ? 0 : count, published);
	}

	private BooleanBuilder buildUnreadPredicate() {
		BooleanBuilder or = new BooleanBuilder();
		or.or(STATUS.read.isNull());
		or.or(STATUS.read.isFalse());

		Instant statusesInstantThreshold = config.database().cleanup().statusesInstantThreshold();
		if (statusesInstantThreshold != null) {
			return or.and(ENTRY.published.goe(statusesInstantThreshold));
		} else {
			return or;
		}
	}

	public long deleteOldStatuses(Instant olderThan, int limit) {
		List<Long> ids = query().select(STATUS.id)
				.from(STATUS)
				.where(STATUS.entryInserted.lt(olderThan), STATUS.starred.isFalse())
				.limit(limit)
				.fetch();
		return deleteQuery(STATUS).where(STATUS.id.in(ids)).execute();
	}

	public long autoMarkAsRead(int limit) {
		Instant now = Instant.now();

		BooleanBuilder where = new BooleanBuilder();
		where.and(SUBSCRIPTION.autoMarkAsReadAfterDays.isNotNull());
		where.and(SUBSCRIPTION.autoMarkAsReadAfterDays.gt(0));

		NumberExpression<Integer> daysDiff = Expressions.numberTemplate(Integer.class, "TIMESTAMPDIFF(DAY, {0}, {1})", ENTRY.published,
				now);
		where.and(daysDiff.goe(SUBSCRIPTION.autoMarkAsReadAfterDays));

		where.and(buildUnreadPredicate());

		List<Tuple> tuples = query().select(ENTRY, STATUS, SUBSCRIPTION)
				.from(ENTRY)
				.join(ENTRY.feed, FEED)
				.join(SUBSCRIPTION)
				.on(SUBSCRIPTION.feed.eq(FEED))
				.leftJoin(ENTRY.statuses, STATUS)
				.on(STATUS.subscription.eq(SUBSCRIPTION))
				.where(where)
				.limit(limit)
				.fetch();

		long updated = 0;

		// Update existing statuses
		List<Long> statusIdsToUpdate = tuples.stream()
				.map(t -> t.get(STATUS))
				.filter(s -> s != null && s.getId() != null)
				.map(FeedEntryStatus::getId)
				.distinct()
				.toList();

		if (!statusIdsToUpdate.isEmpty()) {
			updated += updateQuery(STATUS).where(STATUS.id.in(statusIdsToUpdate)).set(STATUS.read, true).execute();
		}

		// Insert new statuses for entries without existing status
		for (Tuple tuple : tuples) {
			FeedEntryStatus status = tuple.get(STATUS);
			if (status == null || status.getId() == null) {
				FeedEntry entry = tuple.get(ENTRY);
				FeedSubscription sub = tuple.get(SUBSCRIPTION);
				FeedEntryStatus newStatus = new FeedEntryStatus(sub.getUser(), sub, entry);
				newStatus.setRead(true);
				persist(newStatus);
				updated++;
			}
		}

		return updated;
	}

}
