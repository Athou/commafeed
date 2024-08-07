package com.commafeed.backend.dao;

import java.util.List;

import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.QFeedEntry;
import com.commafeed.backend.model.QFeedEntryContent;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLSubQuery;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

@Singleton
public class FeedEntryContentDAO extends GenericDAO<FeedEntryContent> {

	private static final QFeedEntryContent CONTENT = QFeedEntryContent.feedEntryContent;
	private static final QFeedEntry ENTRY = QFeedEntry.feedEntry;

	public FeedEntryContentDAO(EntityManager entityManager) {
		super(entityManager, FeedEntryContent.class);
	}

	public List<FeedEntryContent> findExisting(String contentHash, String titleHash) {
		return query().select(CONTENT).from(CONTENT).where(CONTENT.contentHash.eq(contentHash), CONTENT.titleHash.eq(titleHash)).fetch();
	}

	public long deleteWithoutEntries(int max) {
		JPQLSubQuery<Integer> subQuery = JPAExpressions.selectOne().from(ENTRY).where(ENTRY.content.id.eq(CONTENT.id));
		List<Long> ids = query().select(CONTENT.id).from(CONTENT).where(subQuery.notExists()).limit(max).fetch();

		return deleteQuery(CONTENT).where(CONTENT.id.in(ids)).execute();
	}
}
