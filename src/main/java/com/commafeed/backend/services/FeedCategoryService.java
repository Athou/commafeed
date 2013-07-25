package com.commafeed.backend.services;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.Models;
import com.commafeed.backend.model.User;

public class FeedCategoryService {
	private static Logger log = LoggerFactory.getLogger(FeedSubscriptionService.class);

	@Inject
	FeedCategoryDAO feedCategoryDAO;

	@Inject
	CacheService cache;

	public List<FeedCategory> getCategories(User user) {
		List<FeedCategory> list = cache.getUserCategories(user);
		if (list == null) {
			log.debug("cat list miss for {}", Models.getId(user));
			list = feedCategoryDAO.findAll(user);
			cache.setUserCategories(user, list);
		}
		return list;
	}

}
