package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.ObjectUtils;

import com.commafeed.frontend.utils.ModelFactory.MF;
import com.commafeed.model.FeedCategory;
import com.commafeed.model.FeedSubscription;
import com.commafeed.model.User;
import com.google.common.collect.Lists;
import com.uaihebert.factory.EasyCriteriaFactory;
import com.uaihebert.model.EasyCriteria;

@Stateless
public class FeedSubscriptionService extends GenericDAO<FeedSubscription, Long> {

	@Inject
	FeedCategoryService feedCategoryService;

	public List<FeedSubscription> findAll(User user) {
		return findByField(MF.i(MF.p(FeedCategory.class).getUser()), user);
	}

	public List<FeedSubscription> findWithoutCategories(User user) {
		EasyCriteria<FeedSubscription> criteria = EasyCriteriaFactory
				.createQueryCriteria(em, getType());
		criteria.andEquals("user", user);
		criteria.andEquals("category", null);
		return criteria.getResultList();

	}

	public List<FeedSubscription> findWithCategory(User user,
			FeedCategory category) {

		List<FeedCategory> categories = Lists.newArrayList();
		for (FeedCategory c : feedCategoryService.findAll(user)) {
			if (isChild(c, category)) {
				categories.add(c);
			}
		}

		String query = "select s from FeedSubscription s where s.user=:user and s.category in :categories";
		TypedQuery<FeedSubscription> typedQuery = em.createQuery(query,
				FeedSubscription.class);
		typedQuery.setParameter("user", user);
		typedQuery.setParameter("categories", categories);
		return typedQuery.getResultList();
	}

	private boolean isChild(FeedCategory c, FeedCategory category) {
		boolean isChild = false;
		while (c != null) {
			if (ObjectUtils.equals(c.getId(), category.getId())) {
				isChild = true;
				break;
			}
			c = c.getParent();
		}
		return isChild;
	}

}
