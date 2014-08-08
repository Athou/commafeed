package com.commafeed.backend.dao;

import java.util.List;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.QFeedEntryTag;
import com.commafeed.backend.model.User;
import com.mysema.query.types.ConstructorExpression;

public class FeedEntryTagDAO extends GenericDAO<FeedEntryTag> {

	private QFeedEntryTag tag = QFeedEntryTag.feedEntryTag;

	public FeedEntryTagDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public List<String> findByUser(User user) {
		return newQuery().from(tag).where(tag.user.eq(user)).distinct().list(ConstructorExpression.create(String.class, tag.name));
	}

	public List<FeedEntryTag> findByEntry(User user, FeedEntry entry) {
		return newQuery().from(tag).where(tag.user.eq(user), tag.entry.eq(entry)).list(tag);
	}
}
