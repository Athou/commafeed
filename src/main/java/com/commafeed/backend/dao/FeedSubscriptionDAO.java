package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.FeedSubscription_;
import com.commafeed.backend.model.User;
import com.google.common.collect.Iterables;

@Stateless
public class FeedSubscriptionDAO extends GenericDAO<FeedSubscription> {

	public FeedSubscription findById(User user, Long id) {
		CriteriaQuery<FeedSubscription> query = builder.createQuery(getType());
		Root<FeedSubscription> root = query.from(getType());

		Predicate p1 = builder.equal(root.get(FeedSubscription_.user), user);
		Predicate p2 = builder.equal(root.get(FeedSubscription_.id), id);

		root.fetch(FeedSubscription_.feed, JoinType.LEFT);
		root.fetch(FeedSubscription_.category, JoinType.LEFT);

		query.where(p1, p2);

		return Iterables.getFirst(em.createQuery(query).getResultList(), null);
	}

	public List<FeedSubscription> findByFeed(Feed feed) {
		return findByField(FeedSubscription_.feed, feed);
	}

	public FeedSubscription findByFeed(User user, Feed feed) {

		CriteriaQuery<FeedSubscription> query = builder.createQuery(getType());
		Root<FeedSubscription> root = query.from(getType());

		Predicate p1 = builder.equal(root.get(FeedSubscription_.user), user);
		Predicate p2 = builder.equal(root.get(FeedSubscription_.feed), feed);

		root.fetch(FeedSubscription_.feed, JoinType.LEFT);
		root.fetch(FeedSubscription_.category, JoinType.LEFT);

		query.where(p1, p2);

		return Iterables.getFirst(em.createQuery(query).getResultList(), null);
	}

	public List<FeedSubscription> findAll(User user) {

		CriteriaQuery<FeedSubscription> query = builder.createQuery(getType());
		Root<FeedSubscription> root = query.from(getType());

		root.fetch(FeedSubscription_.feed, JoinType.LEFT);
		root.fetch(FeedSubscription_.category, JoinType.LEFT);

		query.where(builder.equal(root.get(FeedSubscription_.user), user));

		return em.createQuery(query).getResultList();
	}

	public List<FeedSubscription> findByCategory(User user,
			FeedCategory category) {

		CriteriaQuery<FeedSubscription> query = builder.createQuery(getType());
		Root<FeedSubscription> root = query.from(getType());

		Predicate p1 = builder.equal(root.get(FeedSubscription_.user), user);
		Predicate p2 = builder.equal(root.get(FeedSubscription_.category),
				category);

		query.where(p1, p2);

		return em.createQuery(query).getResultList();
	}

	public List<FeedSubscription> findWithoutCategories(User user) {

		CriteriaQuery<FeedSubscription> query = builder.createQuery(getType());
		Root<FeedSubscription> root = query.from(getType());

		Predicate p1 = builder.equal(root.get(FeedSubscription_.user), user);
		Predicate p2 = builder.isNull(root.get(FeedSubscription_.category));

		query.where(p1, p2);

		return em.createQuery(query).getResultList();
	}
}
