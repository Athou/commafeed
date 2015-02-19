package com.commafeed.backend.dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.SessionFactory;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.QFeedEntry;
import com.google.common.collect.Iterables;
import com.mysema.query.Tuple;
import com.mysema.query.types.expr.NumberExpression;

@Singleton
public class FeedEntryDAO extends GenericDAO<FeedEntry> {

	private QFeedEntry entry = QFeedEntry.feedEntry;

	@Inject
	public FeedEntryDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public Long findExisting(String guid, Feed feed) {
		List<Long> list = newQuery().from(entry).where(entry.guidHash.eq(DigestUtils.sha1Hex(guid)), entry.feed.eq(feed)).limit(1)
				.list(entry.id);
		return Iterables.getFirst(list, null);
	}

	public List<FeedCapacity> findFeedsExceedingCapacity(long maxCapacity, long max) {
		NumberExpression<Long> count = entry.id.count();
		List<Tuple> tuples = newQuery().from(entry).groupBy(entry.feed).having(count.gt(maxCapacity)).limit(max).list(entry.feed.id, count);
		return tuples.stream().map(t -> new FeedCapacity(t.get(entry.feed.id), t.get(count))).collect(Collectors.toList());
	}

	public int delete(Long feedId, long max) {
		List<FeedEntry> list = newQuery().from(entry).where(entry.feed.id.eq(feedId)).limit(max).list(entry);
		return delete(list);
	}

	public int deleteOldEntries(Long feedId, long max) {
		List<FeedEntry> list = newQuery().from(entry).where(entry.feed.id.eq(feedId)).orderBy(entry.updated.asc()).limit(max).list(entry);
		return delete(list);
	}

	@AllArgsConstructor
	@Getter
	public static class FeedCapacity {
		private Long id;
		private Long capacity;
	}
}
