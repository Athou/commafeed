package com.commafeed.backend.dao;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.QFeedEntryTag;
import com.commafeed.backend.model.User;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

@Singleton
public class FeedEntryTagDAO extends GenericDAO<FeedEntryTag> {

	private static final QFeedEntryTag TAG = QFeedEntryTag.feedEntryTag;

	public FeedEntryTagDAO(EntityManager entityManager) {
		super(entityManager, FeedEntryTag.class);
	}

	public List<String> findByUser(User user) {
		return query().selectDistinct(TAG.name).from(TAG).where(TAG.user.eq(user)).fetch();
	}

	public List<FeedEntryTag> findByEntry(User user, FeedEntry entry) {
		return query().selectFrom(TAG).where(TAG.user.eq(user), TAG.entry.eq(entry)).fetch();
	}

	public Map<Long, List<FeedEntryTag>> findByEntries(User user, List<FeedEntry> entries) {
		return query().selectFrom(TAG)
				.where(TAG.user.eq(user), TAG.entry.in(entries))
				.fetch()
				.stream()
				.collect(Collectors.groupingBy(t -> t.getEntry().getId()));
	}
}
