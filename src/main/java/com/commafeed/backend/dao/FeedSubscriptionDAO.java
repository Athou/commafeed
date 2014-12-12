package com.commafeed.backend.dao;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.Models;
import com.commafeed.backend.model.QFeedSubscription;
import com.commafeed.backend.model.User;
import com.google.common.collect.Iterables;
import com.mysema.query.jpa.hibernate.HibernateQuery;

@Singleton
public class FeedSubscriptionDAO extends GenericDAO<FeedSubscription> {

	private QFeedSubscription sub = QFeedSubscription.feedSubscription;

	@Inject
	public FeedSubscriptionDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public FeedSubscription findById(User user, Long id) {
		List<FeedSubscription> subs = newQuery().from(sub).where(sub.user.eq(user), sub.id.eq(id)).leftJoin(sub.feed).fetch()
				.leftJoin(sub.category).fetch().list(sub);
		return initRelations(Iterables.getFirst(subs, null));
	}

	public List<FeedSubscription> findByFeed(Feed feed) {
		return newQuery().from(sub).where(sub.feed.eq(feed)).list(sub);
	}

	public FeedSubscription findByFeed(User user, Feed feed) {
		List<FeedSubscription> subs = newQuery().from(sub).where(sub.user.eq(user), sub.feed.eq(feed)).list(sub);
		return initRelations(Iterables.getFirst(subs, null));
	}

	public List<FeedSubscription> findAll(User user) {
		List<FeedSubscription> subs = newQuery().from(sub).where(sub.user.eq(user)).leftJoin(sub.feed).fetch().leftJoin(sub.category)
				.fetch().list(sub);
		return initRelations(subs);
	}

	public List<FeedSubscription> findByCategory(User user, FeedCategory category) {
		HibernateQuery query = newQuery().from(sub).where(sub.user.eq(user));
		if (category == null) {
			query.where(sub.category.isNull());
		} else {
			query.where(sub.category.eq(category));
		}
		return initRelations(query.list(sub));
	}

	public List<FeedSubscription> findByCategories(User user, List<FeedCategory> categories) {
		Set<Long> categoryIds = categories.stream().map(c -> c.getId()).collect(Collectors.toSet());
		return findAll(user).stream().filter(s -> s.getCategory() != null && categoryIds.contains(s.getCategory().getId()))
				.collect(Collectors.toList());
	}

	private List<FeedSubscription> initRelations(List<FeedSubscription> list) {
		list.forEach(s -> initRelations(s));
		return list;
	}

	private FeedSubscription initRelations(FeedSubscription sub) {
		if (sub != null) {
			Models.initialize(sub.getFeed());
			Models.initialize(sub.getCategory());
		}
		return sub;
	}
}
