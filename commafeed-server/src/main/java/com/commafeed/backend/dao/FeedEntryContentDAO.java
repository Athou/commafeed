package com.commafeed.backend.dao;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.QFeedEntry;
import com.commafeed.backend.model.QFeedEntryContent;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

@Singleton
public class FeedEntryContentDAO extends GenericDAO<FeedEntryContent> {

	private final QFeedEntryContent content = QFeedEntryContent.feedEntryContent;
	private final QFeedEntry entry = QFeedEntry.feedEntry;

	@Inject
	public FeedEntryContentDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public List<FeedEntryContent> findExisting(String contentHash, String titleHash) {
		return query().select(content).from(content).where(content.contentHash.eq(contentHash), content.titleHash.eq(titleHash)).fetch();
	}

	public long deleteWithoutEntries(int max) {
		JPQLQuery<Integer> subQuery = JPAExpressions.selectOne().from(entry).where(entry.content.id.eq(content.id));
		List<Long> ids = query().select(content.id).from(content).where(subQuery.notExists()).limit(max).fetch();

		return deleteQuery(content).where(content.id.in(ids)).execute();
	}
}
