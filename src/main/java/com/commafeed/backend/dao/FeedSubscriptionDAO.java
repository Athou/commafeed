package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.FeedSubscription_;
import com.commafeed.backend.model.User;
import com.google.common.collect.Iterables;
import com.uaihebert.factory.EasyCriteriaFactory;
import com.uaihebert.model.EasyCriteria;

@Stateless
public class FeedSubscriptionDAO extends GenericDAO<FeedSubscription> {

	public FeedSubscription findById(User user, Long id) {
		EasyCriteria<FeedSubscription> criteria = createCriteria();
		criteria.andEquals(FeedSubscription_.user.getName(), user);
		criteria.andEquals(FeedSubscription_.id.getName(), id);
		criteria.leftJoinFetch(FeedSubscription_.feed.getName());
		criteria.leftJoinFetch(FeedSubscription_.category.getName());
		return Iterables.getFirst(criteria.getResultList(), null);
	}

	public List<FeedSubscription> findByFeed(Feed feed) {
		EasyCriteria<FeedSubscription> criteria = createCriteria();
		criteria.andEquals(FeedSubscription_.feed.getName(), feed);
		return criteria.getResultList();
	}

	public FeedSubscription findByFeed(User user, Feed feed) {
		EasyCriteria<FeedSubscription> criteria = createCriteria();
		criteria.andEquals(FeedSubscription_.user.getName(), user);
		criteria.andEquals(FeedSubscription_.feed.getName(), feed);
		return Iterables.getFirst(criteria.getResultList(), null);
	}

	public List<FeedSubscription> findAll(User user) {
		EasyCriteria<FeedSubscription> criteria = createCriteria();
		criteria.andEquals(FeedSubscription_.user.getName(), user);

		criteria.innerJoinFetch(FeedSubscription_.feed.getName());
		criteria.leftJoinFetch(FeedSubscription_.category.getName());
		return criteria.getResultList();
	}

	public List<FeedSubscription> findByCategory(User user,
			FeedCategory category) {
		EasyCriteria<FeedSubscription> criteria = EasyCriteriaFactory
				.createQueryCriteria(em, getType());
		criteria.andEquals(FeedSubscription_.user.getName(), user);
		criteria.andEquals(FeedSubscription_.category.getName(), category);
		return criteria.getResultList();

	}

	public List<FeedSubscription> findWithoutCategories(User user) {
		EasyCriteria<FeedSubscription> criteria = EasyCriteriaFactory
				.createQueryCriteria(em, getType());
		criteria.andEquals(FeedSubscription_.user.getName(), user);
		criteria.andIsNull(FeedSubscription_.category.getName());
		return criteria.getResultList();

	}
}
