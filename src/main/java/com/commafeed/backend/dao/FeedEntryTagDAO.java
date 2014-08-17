package com.commafeed.backend.dao;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.QFeedEntryTag;
import com.commafeed.backend.model.User;

@Singleton
public class FeedEntryTagDAO extends GenericDAO<FeedEntryTag> {

	private QFeedEntryTag tag = QFeedEntryTag.feedEntryTag;

	@Inject
	public FeedEntryTagDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public List<String> findByUser(User user) {
		return newQuery().from(tag).where(tag.user.eq(user)).distinct().list(tag.name);
	}

	public List<FeedEntryTag> findByEntry(User user, FeedEntry entry) {
		return newQuery().from(tag).where(tag.user.eq(user), tag.entry.eq(entry)).list(tag);
	}
}
