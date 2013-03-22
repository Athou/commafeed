package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;

import com.commafeed.frontend.utils.ModelFactory.MF;
import com.commafeed.model.FeedCategory;
import com.commafeed.model.FeedSubscription;
import com.commafeed.model.User;
import com.uaihebert.factory.EasyCriteriaFactory;
import com.uaihebert.model.EasyCriteria;

@Stateless
public class FeedSubscriptionService extends GenericDAO<FeedSubscription, Long> {

	public List<FeedSubscription> findAll(User user) {
		return findByField(MF.i(MF.p(FeedCategory.class).getUser()), user);
	}
	
	public List<FeedSubscription> findWithoutCategories(User user) {
		EasyCriteria<FeedSubscription> criteria = EasyCriteriaFactory.createQueryCriteria(em, getType());
		criteria.andEquals("user", user);
		criteria.andEquals("category", null);
		return criteria.getResultList();
		
	}
}
