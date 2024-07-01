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

	private final FeedEntryTagDAO feedEntryTagDAO;
	private final CommaFeedConfiguration config;

	private final QFeedEntryStatus status = QFeedEntryStatus.feedEntryStatus;
	private final QFeedEntry entry = QFeedEntry.feedEntry;
	private final QFeed feed = QFeed.feed;
	private final QFeedSubscription subscription = QFeedSubscription.feedSubscription;
	private final QFeedEntryContent content = QFeedEntryContent.feedEntryContent;
	private final QFeedEntryTag entryTag = QFeedEntryTag.feedEntryTag;

	@Inject
	public FeedEntryStatusDAO(SessionFactory sessionFactory, FeedEntryTagDAO feedEntryTagDAO, CommaFeedConfiguration config) {
		super(sessionFactory);
		this.feedEntryTagDAO = feedEntryTagDAO;
		this.config = config;
	}

	public FeedEntryStatus getStatus(User user, FeedSubscription sub, FeedEntry entry) {
		List<FeedEntryStatus> statuses = query().selectFrom(status).where(status.entry.eq(entry), status.subscription.eq(sub)).fetch();
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
		JPAQuery<FeedEntryStatus> query = query().selectFrom(status).where(status.user.eq(user), status.starred.isTrue());
		if (includeContent) {
			query.join(status.entry.content).fetchJoin();
		}

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
		statuses.forEach(s -> s.setMarkable(true));
		if (includeContent) {
			fetchTags(user, statuses);
		}

		return statuses;
	}

	public List<FeedEntryStatus> findBySubscriptions(User user, List<FeedSubscription> subs, boolean unreadOnly,
			List<FeedEntryKeyword> keywords, Instant newerThan, int offset, int limit, ReadingOrder order, boolean includeContent,
			String tag, Long minEntryId, Long maxEntryId) {

		JPAQuery<Tuple> query = query().select(entry, subscription, status).from(entry);
		query.join(entry.feed, feed);
		query.join(subscription).on(subscription.feed.eq(feed).and(subscription.user.eq(user)));
		query.leftJoin(status).on(status.entry.eq(entry).and(status.subscription.eq(subscription)));
		query.where(subscription.in(subs));

		if (includeContent || CollectionUtils.isNotEmpty(keywords)) {
			query.join(entry.content, content).fetchJoin();
		}
		if (CollectionUtils.isNotEmpty(keywords)) {
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

		if (unreadOnly && tag == null) {
			query.where(buildUnreadPredicate());
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

		List<FeedEntryStatus> statuses = new ArrayList<>();
		List<Tuple> tuples = query.fetch();
		for (Tuple tuple : tuples) {
			FeedEntry e = tuple.get(entry);
			FeedSubscription sub = tuple.get(subscription);
			FeedEntryStatus s = handleStatus(user, tuple.get(status), sub, e);
			statuses.add(s);
		}

		if (includeContent) {
			fetchTags(user, statuses);
		}

		return statuses;
	}

	public UnreadCount getUnreadCount(FeedSubscription sub) {
		JPAQuery<Tuple> query = query().select(entry.count(), entry.updated.max())
				.from(entry)
				.join(entry.feed, feed)
				.join(subscription)
				.on(subscription.feed.eq(feed).and(subscription.eq(sub)))
				.leftJoin(status)
				.on(status.entry.eq(entry).and(status.subscription.eq(sub)))
				.where(buildUnreadPredicate());

		Tuple tuple = query.fetchOne();
		Long count = tuple.get(entry.count());
		Instant updated = tuple.get(entry.updated.max());
		return new UnreadCount(sub.getId(), count == null ? 0 : count, updated);
	}

	private BooleanBuilder buildUnreadPredicate() {
		BooleanBuilder or = new BooleanBuilder();
		or.or(status.read.isNull());
		or.or(status.read.isFalse());

		Instant unreadThreshold = config.getApplicationSettings().getUnreadThreshold();
		if (unreadThreshold != null) {
			return or.and(entry.updated.goe(unreadThreshold));
		} else {
			return or;
		}
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
