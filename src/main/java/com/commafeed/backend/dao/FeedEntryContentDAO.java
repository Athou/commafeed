package com.commafeed.backend.dao;

import java.util.List;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.QFeedEntry;
import com.commafeed.backend.model.QFeedEntryContent;
import com.google.common.collect.Iterables;

public class FeedEntryContentDAO extends GenericDAO<FeedEntryContent> {

	private QFeedEntryContent content = QFeedEntryContent.feedEntryContent;

	public FeedEntryContentDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public Long findExisting(String contentHash, String titleHash) {
		List<Long> list = newQuery().from(content).where(content.contentHash.eq(contentHash), content.titleHash.eq(titleHash)).limit(1)
				.list(content.id);
		return Iterables.getFirst(list, null);
	}

	public int deleteWithoutEntries(int max) {
		QFeedEntry entry = QFeedEntry.feedEntry;
		List<FeedEntryContent> list = newQuery().from(content).leftJoin(content.entries, entry).where(entry.id.isNull()).limit(max)
				.list(content);
		int deleted = list.size();
		delete(list);
		return deleted;
	}
}
