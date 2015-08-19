package com.commafeed.backend.dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.SessionFactory;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.QFeedEntry;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberExpression;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Singleton
public class FeedEntryDAO extends GenericDAO<FeedEntry> {

	private QFeedEntry entry = QFeedEntry.feedEntry;

	@Inject
	public FeedEntryDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public Long findExisting(String guid, Feed feed) {
		return query().select(entry.id).from(entry).where(entry.guidHash.eq(DigestUtils.sha1Hex(guid)), entry.feed.eq(feed)).limit(1)
				.fetchOne();
	}

	public List<FeedCapacity> findFeedsExceedingCapacity(long maxCapacity, long max) {
		NumberExpression<Long> count = entry.id.count();
		List<Tuple> tuples = query().select(entry.feed.id, count).from(entry).groupBy(entry.feed).having(count.gt(maxCapacity)).limit(max)
				.fetch();
		return tuples.stream().map(t -> new FeedCapacity(t.get(entry.feed.id), t.get(count))).collect(Collectors.toList());
	}

	public int delete(Long feedId, long max) {

		List<FeedEntry> list = query().selectFrom(entry).where(entry.feed.id.eq(feedId)).limit(max).fetch();
		return delete(list);
	}

	public int deleteOldEntries(Long feedId, long max) {
		List<FeedEntry> list = query().selectFrom(entry).where(entry.feed.id.eq(feedId)).orderBy(entry.updated.asc()).limit(max).fetch();
		return delete(list);
	}

	@AllArgsConstructor
	@Getter
	public static class FeedCapacity {
		private Long id;
		private Long capacity;
	}
}
