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
import com.commafeed.backend.model.QFeedEntry;
import com.commafeed.backend.model.QFeedEntryContent;
import com.commafeed.backend.model.QFeedEntryStatus;
import com.commafeed.backend.model.QFeedEntryTag;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.frontend.model.UnreadCount;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;

@Singleton
public class FeedEntryStatusDAO extends GenericDAO<FeedEntryStatus> {

	private static final QFeedEntryStatus STATUS = QFeedEntryStatus.feedEntryStatus;
	private static final QFeedEntry ENTRY = QFeedEntry.feedEntry;
	private static final QFeedEntryContent CONTENT = QFeedEntryContent.feedEntryContent;
	private static final QFeedEntryTag TAG = QFeedEntryTag.feedEntryTag;

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

	public void updateAutoMarkAsReadDate(FeedSubscription sub, Instant autoMarkAsReadAfter) {
		/*
		 * Support for the auto-mark-read feature: update the auto_mark_as_read_after
		 * column for all unread entries belonging to the given
		 * subscription.
		 */
		updateQuery(STATUS).set(STATUS.autoMarkAsReadAfter, autoMarkAsReadAfter)
				.where(STATUS.subscription.eq(sub), STATUS.read.isFalse())
				.execute();
	}

	public long markExpiredAutoMarkAsReadStatuses(Instant now, int limit) {
		/*
		 * Support for the auto-mark-read feature: find unread entries whose expiration
		 * date has passed and mark them as read. Uses batching
		 * for performance.
		 */
		List<Long> ids = query().select(STATUS.id)
				.from(STATUS)
				.where(STATUS.read.isFalse(), STATUS.autoMarkAsReadAfter.lt(now))
				.limit(limit)
				.fetch();
		if (ids.isEmpty()) {
			return 0;
		}
		return updateQuery(STATUS).set(STATUS.read, true).where(STATUS.id.in(ids)).execute();
	}

	/**
	 * Support for the auto-mark-read feature: resets expiration dates effectively disabling the feature for a subscription.
	 * 
	 * @param sub
	 *            the subscription to reset
	 */
	public void resetAutoMarkAsReadStatuses(FeedSubscription sub) {
		/*
		 * Support for the auto-mark-read feature: clear auto_mark_as_read_after and
		 * delete unneeded status records.
		 */

		// 1. Clear the expiration timestamp for all entries in this subscription
		updateQuery(STATUS).set(STATUS.autoMarkAsReadAfter, (Instant) null).where(STATUS.subscription.eq(sub)).execute();

		// 2. Delete rows that are only here because of the auto-mark-read feature
		// (unread and non-starred) to revert to the original "no-status" state for
		// unread entries.
		//
		// Removed per Jerome comment
		// deleteQuery(STATUS).where(STATUS.subscription.eq(sub), STATUS.read.isFalse(),
		// STATUS.starred.isFalse()).execute();
	}

}
