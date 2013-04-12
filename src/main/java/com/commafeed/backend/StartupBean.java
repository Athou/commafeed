package com.commafeed.backend;

import java.util.Arrays;
import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.feeds.FeedRefreshWorker;
import com.commafeed.backend.model.ApplicationSettings;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.backend.services.UserService;

@Startup
@Singleton
public class StartupBean {

	private static Logger log = LoggerFactory.getLogger(StartupBean.class);
	public static final String ADMIN_NAME = "admin";

	@Inject
	FeedDAO feedDAO;

	@Inject
	FeedCategoryDAO feedCategoryDAO;

	@Inject
	FeedSubscriptionDAO feedSubscriptionDAO;

	@Inject
	UserDAO userDAO;

	@Inject
	UserService userService;

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Inject
	FeedRefreshWorker worker;

	private long startupTime;

	@PostConstruct
	private void init() {
		startupTime = Calendar.getInstance().getTimeInMillis();
		if (userDAO.getCount() == 0) {
			initialData();
		}

		ApplicationSettings settings = applicationSettingsService.get();
		for (int i = 0; i < settings.getBackgroundThreads(); i++) {
			worker.start();
		}

	}

	private void initialData() {
		log.info("Populating database with default values");

		applicationSettingsService.save(new ApplicationSettings());

		User user = userService.register(ADMIN_NAME, "admin",
				Arrays.asList(Role.ADMIN, Role.USER));
		userService.register("test", "test", Arrays.asList(Role.USER));

		Feed dilbert = new Feed("http://feed.dilbert.com/dilbert/daily_strip");
		feedDAO.save(dilbert);

		Feed engadget = new Feed("http://www.engadget.com/rss.xml");
		feedDAO.save(engadget);

		Feed frandroid = new Feed("http://feeds.feedburner.com/frandroid");
		feedDAO.save(frandroid);

		FeedCategory newsCategory = new FeedCategory();
		newsCategory.setName("News");
		newsCategory.setUser(user);
		feedCategoryDAO.save(newsCategory);

		FeedCategory comicsCategory = new FeedCategory();
		comicsCategory.setName("Comics");
		comicsCategory.setUser(user);
		comicsCategory.setParent(newsCategory);
		feedCategoryDAO.save(comicsCategory);

		FeedCategory techCategory = new FeedCategory();
		techCategory.setName("Tech");
		techCategory.setUser(user);
		techCategory.setParent(newsCategory);
		feedCategoryDAO.save(techCategory);

		FeedSubscription sub = new FeedSubscription();
		sub.setCategory(comicsCategory);
		sub.setFeed(dilbert);
		sub.setTitle("Dilbert - Strips");
		sub.setUser(user);
		feedSubscriptionDAO.save(sub);

		FeedSubscription sub2 = new FeedSubscription();
		sub2.setCategory(techCategory);
		sub2.setFeed(engadget);
		sub2.setTitle("Engadget");
		sub2.setUser(user);
		feedSubscriptionDAO.save(sub2);

		FeedSubscription sub3 = new FeedSubscription();
		sub3.setFeed(frandroid);
		sub3.setTitle("Frandroid");
		sub3.setUser(user);
		feedSubscriptionDAO.save(sub3);
	}

	public long getStartupTime() {
		return startupTime;
	}

}
