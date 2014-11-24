package com.commafeed.backend.dao;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.SessionFactory;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.QFeedEntry;
import com.google.common.collect.Iterables;

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

	public int delete(Date olderThan, int max) {
		List<FeedEntry> list = newQuery().from(entry).where(entry.inserted.lt(olderThan)).limit(max).list(entry);
		int deleted = list.size();
		delete(list);
		return deleted;
	}
}
