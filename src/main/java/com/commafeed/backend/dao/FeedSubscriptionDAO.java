package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Hibernate;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedCategory_;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.FeedSubscription_;
import com.commafeed.backend.model.Feed_;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.User_;
import com.google.common.collect.Iterables;

@Stateless
public class FeedSubscriptionDAO extends GenericDAO<FeedSubscription> {

	public FeedSubscription findById(User user, Long id) {
		CriteriaQuery<FeedSubscription> query = builder.createQuery(getType());
		Root<FeedSubscription> root = query.from(getType());

		Predicate p1 = builder.equal(
				root.get(FeedSubscription_.user).get(User_.id), user.getId());
		Predicate p2 = builder.equal(root.get(FeedSubscription_.id), id);

		root.fetch(FeedSubscription_.feed, JoinType.LEFT);
		root.fetch(FeedSubscription_.category, JoinType.LEFT);

		query.where(p1, p2);

		FeedSubscription sub = Iterables.getFirst(cache(em.createQuery(query))
				.getResultList(), null);
		initRelations(sub);
		return sub;
	}

	public List<FeedSubscription> findByFeed(Feed feed) {
		CriteriaQuery<FeedSubscription> query = builder.createQuery(getType());
		Root<FeedSubscription> root = query.from(getType());

		query.where(builder.equal(root.get(FeedSubscription_.feed)
				.get(Feed_.id), feed.getId()));
		List<FeedSubscription> list = cache(em.createQuery(query))
				.getResultList();
		initRelations(list);
		return list;
	}

	public FeedSubscription findByFeed(User user, Feed feed) {

		CriteriaQuery<FeedSubscription> query = builder.createQuery(getType());
		Root<FeedSubscription> root = query.from(getType());

		Predicate p1 = builder.equal(
				root.get(FeedSubscription_.user).get(User_.id), user.getId());
		Predicate p2 = builder.equal(
				root.get(FeedSubscription_.feed).get(Feed_.id), feed.getId());

		root.fetch(FeedSubscription_.feed, JoinType.LEFT);
		root.fetch(FeedSubscription_.category, JoinType.LEFT);

		query.where(p1, p2);

		FeedSubscription sub = Iterables.getFirst(cache(em.createQuery(query))
				.getResultList(), null);
		initRelations(sub);
		return sub;
	}

	public List<FeedSubscription> findAll(User user) {

		CriteriaQuery<FeedSubscription> query = builder.createQuery(getType());
		Root<FeedSubscription> root = query.from(getType());

		root.fetch(FeedSubscription_.feed, JoinType.LEFT);
		root.fetch(FeedSubscription_.category, JoinType.LEFT);

		query.where(builder.equal(root.get(FeedSubscription_.user)
				.get(User_.id), user.getId()));

		List<FeedSubscription> list = cache(em.createQuery(query))
				.getResultList();
		initRelations(list);
		return list;
	}

	public List<FeedSubscription> findByCategory(User user,
			FeedCategory category) {

		CriteriaQuery<FeedSubscription> query = builder.createQuery(getType());
		Root<FeedSubscription> root = query.from(getType());

		Predicate p1 = builder.equal(
				root.get(FeedSubscription_.user).get(User_.id), user.getId());
		Predicate p2 = null;
		if (category == null) {
			p2 = builder.isNull(
					root.get(FeedSubscription_.category));
		} else {
			p2 = builder.equal(
					root.get(FeedSubscription_.category).get(FeedCategory_.id),
					category.getId());

		}

		query.where(p1, p2);

		List<FeedSubscription> list = cache(em.createQuery(query))
				.getResultList();
		initRelations(list);
		return list;
	}

	private void initRelations(List<FeedSubscription> list) {
		for (FeedSubscription sub : list) {
			initRelations(sub);
		}
	}

	private void initRelations(FeedSubscription sub) {
		if (sub != null) {
			Hibernate.initialize(sub.getFeed());
			Hibernate.initialize(sub.getCategory());
		}
	}
}
