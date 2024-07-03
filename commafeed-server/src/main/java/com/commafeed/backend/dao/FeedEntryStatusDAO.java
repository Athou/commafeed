package com.commafeed.backend.dao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.SessionFactory;

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
import com.google.common.collect.Iterables;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class FeedEntryStatusDAO extends GenericDAO<FeedEntryStatus> {

	private static final QFeedEntryStatus STATUS = QFeedEntryStatus.feedEntryStatus;
	private static final QFeedEntry ENTRY = QFeedEntry.feedEntry;
	private static final QFeed FEED = QFeed.feed;
	private static final QFeedSubscription SUBSCRIPTION = QFeedSubscription.feedSubscription;
	private static final QFeedEntryContent CONTENT = QFeedEntryContent.feedEntryContent;
	private static final QFeedEntryTag TAG = QFeedEntryTag.feedEntryTag;

	private final FeedEntryTagDAO feedEntryTagDAO;
	private final CommaFeedConfiguration config;

	@Inject
	public FeedEntryStatusDAO(SessionFactory sessionFactory, FeedEntryTagDAO feedEntryTagDAO, CommaFeedConfiguration config) {
		super(sessionFactory);
		this.feedEntryTagDAO = feedEntryTagDAO;
		this.config = config;
	}

	public FeedEntryStatus getStatus(User user, FeedSubscription sub, FeedEntry entry) {
		List<FeedEntryStatus> statuses = query().selectFrom(STATUS).where(STATUS.entry.eq(entry), STATUS.subscription.eq(sub)).fetch();
		FeedEntryStatus status = Iterables.getFirst(statuses, null);
		return handleStatus(user, status, sub, entry);
	}

	/**
	 * creates an artificial "unread" status if status is null
	 */
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
			query.join(STATUS.entry.content).fetchJoin();
		}

		if (newerThan != null) {
			query.where(STATUS.entryInserted.gt(newerThan));
		}

		if (order == ReadingOrder.asc) {
			query.orderBy(STATUS.entryUpdated.asc(), STATUS.id.asc());
		} else {
			query.orderBy(STATUS.entryUpdated.desc(), STATUS.id.desc());
		}

		if (offset > -1) {
			query.offset(offset);
		}

		if (limit > -1) {
			query.limit(limit);
		}

		setTimeout(query, config.getApplicationSettings().getQueryTimeout());

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

		JPAQuery<Tuple> query = query().select(ENTRY, SUBSCRIPTION, STATUS).from(ENTRY);
		query.join(ENTRY.feed, FEED);
		query.join(SUBSCRIPTION).on(SUBSCRIPTION.feed.eq(FEED).and(SUBSCRIPTION.user.eq(user)));
		query.leftJoin(STATUS).on(STATUS.entry.eq(ENTRY).and(STATUS.subscription.eq(SUBSCRIPTION)));
		query.where(SUBSCRIPTION.in(subs));

		if (includeContent || CollectionUtils.isNotEmpty(keywords)) {
			query.join(ENTRY.content, CONTENT).fetchJoin();
		}
		if (CollectionUtils.isNotEmpty(keywords)) {
			for (FeedEntryKeyword keyword : keywords) {
				BooleanBuilder or = new BooleanBuilder();
				or.or(CONTENT.content.containsIgnoreCase(keyword.getKeyword()));
				or.or(CONTENT.title.containsIgnoreCase(keyword.getKeyword()));
				if (keyword.getMode() == Mode.EXCLUDE) {
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
			if (order == ReadingOrder.asc) {
				query.orderBy(ENTRY.updated.asc(), ENTRY.id.asc());
			} else {
				query.orderBy(ENTRY.updated.desc(), ENTRY.id.desc());
			}
		}

		if (offset > -1) {
			query.offset(offset);
		}

		if (limit > -1) {
			query.limit(limit);
		}

		setTimeout(query, config.getApplicationSettings().getQueryTimeout());

		List<FeedEntryStatus> statuses = new ArrayList<>();
		List<Tuple> tuples = query.fetch();
		for (Tuple tuple : tuples) {
			FeedEntry e = tuple.get(ENTRY);
			FeedSubscription sub = tuple.get(SUBSCRIPTION);
			FeedEntryStatus s = handleStatus(user, tuple.get(STATUS), sub, e);
			statuses.add(s);
		}

		if (includeContent) {
			fetchTags(user, statuses);
		}

		return statuses;
	}

	public UnreadCount getUnreadCount(FeedSubscription sub) {
		JPAQuery<Tuple> query = query().select(ENTRY.count(), ENTRY.updated.max())
				.from(ENTRY)
				.join(ENTRY.feed, FEED)
				.join(SUBSCRIPTION)
				.on(SUBSCRIPTION.feed.eq(FEED).and(SUBSCRIPTION.eq(sub)))
				.leftJoin(STATUS)
				.on(STATUS.entry.eq(ENTRY).and(STATUS.subscription.eq(sub)))
				.where(buildUnreadPredicate());

		Tuple tuple = query.fetchOne();
		Long count = tuple.get(ENTRY.count());
		Instant updated = tuple.get(ENTRY.updated.max());
		return new UnreadCount(sub.getId(), count == null ? 0 : count, updated);
	}

	private BooleanBuilder buildUnreadPredicate() {
		BooleanBuilder or = new BooleanBuilder();
		or.or(STATUS.read.isNull());
		or.or(STATUS.read.isFalse());

		Instant unreadThreshold = config.getApplicationSettings().getUnreadThreshold();
		if (unreadThreshold != null) {
			return or.and(ENTRY.updated.goe(unreadThreshold));
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

}
