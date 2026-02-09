package com.commafeed.backend.dao;

import java.time.Instant;
import java.util.List;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.QFeedEntry;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;

@Singleton
public class FeedEntryDAO extends GenericDAO<FeedEntry> {

	private static final QFeedEntry ENTRY = QFeedEntry.feedEntry;

	public FeedEntryDAO(EntityManager entityManager) {
		super(entityManager, FeedEntry.class);
	}

	public FeedEntry findExisting(String guidHash, Feed feed) {
		return query().select(ENTRY).from(ENTRY).where(ENTRY.guidHash.eq(guidHash), ENTRY.feed.eq(feed)).limit(1).fetchOne();
	}

	public List<FeedCapacity> findFeedsExceedingCapacity(long maxCapacity, long max, boolean keepStarredEntries) {
		NumberExpression<Long> count = ENTRY.id.count();
		JPAQuery<Tuple> query = query().select(ENTRY.feed.id, count).from(ENTRY);

		if (keepStarredEntries) {
			query.where(Predicates.isNotStarred(ENTRY));
		}

		return query.groupBy(ENTRY.feed)
				.having(count.gt(maxCapacity))
				.limit(max)
				.fetch()
				.stream()
				.map(t -> new FeedCapacity(t.get(ENTRY.feed.id), t.get(count)))
				.toList();
	}

	public int delete(Long feedId, long max) {
		List<FeedEntry> list = query().selectFrom(ENTRY).where(ENTRY.feed.id.eq(feedId)).limit(max).fetch();
		return delete(list);
	}

	/**
	 * Delete entries older than a certain date
	 */
	public int deleteEntriesOlderThan(Instant olderThan, long max, boolean keepStarredEntries) {
		JPAQuery<FeedEntry> query = query().selectFrom(ENTRY)
				.where(ENTRY.published.lt(olderThan))
				.orderBy(ENTRY.published.asc())
				.limit(max);

		if (keepStarredEntries) {
			query.where(Predicates.isNotStarred(ENTRY));
		}

		return delete(query.fetch());
	}

	/**
	 * Delete the oldest entries of a feed
	 */
	public int deleteOldEntries(Long feedId, long max, boolean keepStarredEntries) {
		JPAQuery<FeedEntry> query = query().selectFrom(ENTRY).where(ENTRY.feed.id.eq(feedId)).orderBy(ENTRY.published.asc()).limit(max);

		if (keepStarredEntries) {
			query.where(Predicates.isNotStarred(ENTRY));
		}

		return delete(query.fetch());
	}

	public record FeedCapacity(Long id, Long capacity) {
	}
}
