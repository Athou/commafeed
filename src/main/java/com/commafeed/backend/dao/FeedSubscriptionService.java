package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.common.collect.Iterables;
import com.uaihebert.factory.EasyCriteriaFactory;
import com.uaihebert.model.EasyCriteria;

@Stateless
@SuppressWarnings("serial")
public class FeedSubscriptionService extends GenericDAO<FeedSubscription> {

	@Inject
	FeedCategoryService feedCategoryService;

	public FeedSubscription findById(User user, Long id) {
		EasyCriteria<FeedSubscription> criteria = createCriteria();
		criteria.andEquals(MF.i(proxy().getUser()), user);
		criteria.andEquals(MF.i(proxy().getId()), id);
		return Iterables.getFirst(criteria.getResultList(), null);
	}

	public List<FeedSubscription> findByFeed(Feed feed) {
		EasyCriteria<FeedSubscription> criteria = createCriteria();
		criteria.andEquals(MF.i(proxy().getFeed()), feed);
		return criteria.getResultList();
	}

	public FeedSubscription findByFeed(User user, Feed feed) {
		EasyCriteria<FeedSubscription> criteria = createCriteria();
		criteria.andEquals(MF.i(proxy().getUser()), user);
		criteria.andEquals(MF.i(proxy().getFeed()), feed);
		return Iterables.getFirst(criteria.getResultList(), null);
	}

	public List<FeedSubscription> findAll(User user) {
		EasyCriteria<FeedSubscription> criteria = createCriteria();
		criteria.andEquals(MF.i(proxy().getUser()), user);
		
		criteria.innerJoinFetch(MF.i(proxy().getFeed()));
		criteria.innerJoinFetch(MF.i(proxy().getUser()));
		criteria.leftJoinFetch(MF.i(proxy().getCategory()));
		return criteria.getResultList();
	}

	public List<FeedSubscription> findWithoutCategories(User user) {
		EasyCriteria<FeedSubscription> criteria = EasyCriteriaFactory
				.createQueryCriteria(em, getType());
		criteria.andEquals(MF.i(proxy().getUser()), user);
		criteria.andIsNull(MF.i(proxy().getCategory()));
		return criteria.getResultList();

	}
}
