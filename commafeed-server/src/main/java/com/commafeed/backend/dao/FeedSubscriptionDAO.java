package com.commafeed.backend.dao;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;

import com.commafeed.backend.model.AbstractModel;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.Models;
import com.commafeed.backend.model.QFeedSubscription;
import com.commafeed.backend.model.User;
import com.google.common.collect.Iterables;
import com.querydsl.jpa.JPQLQuery;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class FeedSubscriptionDAO extends GenericDAO<FeedSubscription> {

	private static final QFeedSubscription SUBSCRIPTION = QFeedSubscription.feedSubscription;

	private final SessionFactory sessionFactory;

	@Inject
	public FeedSubscriptionDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
		this.sessionFactory = sessionFactory;
	}

	public void onPostCommitInsert(Consumer<FeedSubscription> consumer) {
		sessionFactory.unwrap(SessionFactoryImplementor.class)
				.getServiceRegistry()
				.getService(EventListenerRegistry.class)
				.getEventListenerGroup(EventType.POST_COMMIT_INSERT)
				.appendListener(new PostInsertEventListener() {
					@Override
					public void onPostInsert(PostInsertEvent event) {
						if (event.getEntity() instanceof FeedSubscription s) {
							consumer.accept(s);
						}
					}

					@Override
					public boolean requiresPostCommitHandling(EntityPersister persister) {
						return true;
					}
				});
	}

	public FeedSubscription findById(User user, Long id) {
		List<FeedSubscription> subs = query().selectFrom(SUBSCRIPTION)
				.where(SUBSCRIPTION.user.eq(user), SUBSCRIPTION.id.eq(id))
				.leftJoin(SUBSCRIPTION.feed)
				.fetchJoin()
				.leftJoin(SUBSCRIPTION.category)
				.fetchJoin()
				.fetch();
		return initRelations(Iterables.getFirst(subs, null));
	}

	public List<FeedSubscription> findByFeed(Feed feed) {
		return query().selectFrom(SUBSCRIPTION).where(SUBSCRIPTION.feed.eq(feed)).fetch();
	}

	public FeedSubscription findByFeed(User user, Feed feed) {
		List<FeedSubscription> subs = query().selectFrom(SUBSCRIPTION)
				.where(SUBSCRIPTION.user.eq(user), SUBSCRIPTION.feed.eq(feed))
				.fetch();
		return initRelations(Iterables.getFirst(subs, null));
	}

	public List<FeedSubscription> findAll(User user) {
		List<FeedSubscription> subs = query().selectFrom(SUBSCRIPTION)
				.where(SUBSCRIPTION.user.eq(user))
				.leftJoin(SUBSCRIPTION.feed)
				.fetchJoin()
				.leftJoin(SUBSCRIPTION.category)
				.fetchJoin()
				.fetch();
		return initRelations(subs);
	}

	public Long count(User user) {
		return query().select(SUBSCRIPTION.count()).from(SUBSCRIPTION).where(SUBSCRIPTION.user.eq(user)).fetchOne();
	}

	public List<FeedSubscription> findByCategory(User user, FeedCategory category) {
		JPQLQuery<FeedSubscription> query = query().selectFrom(SUBSCRIPTION).where(SUBSCRIPTION.user.eq(user));
		if (category == null) {
			query.where(SUBSCRIPTION.category.isNull());
		} else {
			query.where(SUBSCRIPTION.category.eq(category));
		}
		return initRelations(query.fetch());
	}

	public List<FeedSubscription> findByCategories(User user, List<FeedCategory> categories) {
		Set<Long> categoryIds = categories.stream().map(AbstractModel::getId).collect(Collectors.toSet());
		return findAll(user).stream().filter(s -> s.getCategory() != null && categoryIds.contains(s.getCategory().getId())).toList();
	}

	private List<FeedSubscription> initRelations(List<FeedSubscription> list) {
		list.forEach(this::initRelations);
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
