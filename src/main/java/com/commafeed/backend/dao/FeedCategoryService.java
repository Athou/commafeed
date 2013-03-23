package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;

import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.utils.ModelFactory.MF;

@Stateless
public class FeedCategoryService extends GenericDAO<FeedCategory, Long> {

	public List<FeedCategory> findAll(User user) {
		return findByField(MF.i(MF.p(FeedCategory.class).getUser()), user);
	}
}
