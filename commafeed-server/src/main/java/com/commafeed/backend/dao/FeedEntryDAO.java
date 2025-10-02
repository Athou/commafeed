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

@Singleton
public class FeedEntryDAO extends GenericDAO<FeedEntry> {

	private static final QFeedEntry ENTRY = QFeedEntry.feedEntry;

	public FeedEntryDAO(EntityManager entityManager) {
		super(entityManager, FeedEntry.class);
	}

	public FeedEntry findExisting(String guidHash, Feed feed) {
		return query().select(ENTRY).from(ENTRY).where(ENTRY.guidHash.eq(guidHash), ENTRY.feed.eq(feed)).limit(1).fetchOne();
	}

	public List<FeedCapacity> findFeedsExceedingCapacity(long maxCapacity, long max) {
		NumberExpression<Long> count = ENTRY.id.count();
		List<Tuple> tuples = query().select(ENTRY.feed.id, count)
				.from(ENTRY)
				.groupBy(ENTRY.feed)
				.having(count.gt(maxCapacity))
				.limit(max)
				.fetch();
		return tuples.stream().map(t -> new FeedCapacity(t.get(ENTRY.feed.id), t.get(count))).toList();
	}

	public int delete(Long feedId, long max) {
		List<FeedEntry> list = query().selectFrom(ENTRY).where(ENTRY.feed.id.eq(feedId)).limit(max).fetch();
		return delete(list);
	}

	/**
	 * Delete entries older than a certain date
	 */
	public int deleteEntriesOlderThan(Instant olderThan, long max) {
		List<FeedEntry> list = query().selectFrom(ENTRY)
				.where(ENTRY.published.lt(olderThan))
				.orderBy(ENTRY.published.asc())
				.limit(max)
				.fetch();
		return delete(list);
	}

	/**
	 * Delete the oldest entries of a feed
	 */
	public int deleteOldEntries(Long feedId, long max) {
		List<FeedEntry> list = query().selectFrom(ENTRY).where(ENTRY.feed.id.eq(feedId)).orderBy(ENTRY.published.asc()).limit(max).fetch();
		return delete(list);
	}

	public record FeedCapacity(Long id, Long capacity) {
	}
}
