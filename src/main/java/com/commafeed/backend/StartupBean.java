package com.commafeed.backend;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.FeedCategoryService;
import com.commafeed.backend.dao.FeedService;
import com.commafeed.backend.dao.FeedSubscriptionService;
import com.commafeed.backend.dao.UserService;
import com.commafeed.backend.security.PasswordEncryptionService;
import com.commafeed.model.Feed;
import com.commafeed.model.FeedCategory;
import com.commafeed.model.FeedSubscription;
import com.commafeed.model.User;

@Startup
@Singleton
public class StartupBean {

	private static Logger log = LoggerFactory.getLogger(StartupBean.class);

	@Inject
	FeedService feedService;

	@Inject
	FeedCategoryService feedCategoryService;

	@Inject
	FeedSubscriptionService feedSubscriptionService;

	@Inject
	UserService userService;

	@Inject
	PasswordEncryptionService encryptionService;

	@PostConstruct
	private void init() {

		if (userService.getCount() == 0) {
			log.info("Populating database with default values");
			User user = new User();
			byte[] salt = encryptionService.generateSalt();
			user.setName("admin");
			user.setSalt(salt);
			user.setPassword(encryptionService.getEncryptedPassword("admin",
					salt));
			userService.save(user);

			Feed feed = new Feed("http://feed.dilbert.com/dilbert/daily_strip");
			feedService.save(feed);

			FeedCategory newsCategory = new FeedCategory();
			newsCategory.setName("News");
			newsCategory.setUser(user);
			feedCategoryService.save(newsCategory);

			FeedCategory comicsCategory = new FeedCategory();
			comicsCategory.setName("Comics");
			comicsCategory.setUser(user);
			comicsCategory.setParent(newsCategory);
			feedCategoryService.save(comicsCategory);

			FeedSubscription sub = new FeedSubscription();
			sub.setCategory(comicsCategory);
			sub.setFeed(feed);
			sub.setTitle("Dilbert - Strips");
			sub.setUser(user);
			feedSubscriptionService.save(sub);

		}

	}

}
