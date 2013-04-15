package com.commafeed.backend;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
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
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.backend.services.UserService;
import com.google.api.client.util.Lists;

@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
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

	private List<Future<Void>> threads = Lists.newArrayList();

	private long startupTime;

	@PostConstruct
	private void init() {
		startupTime = Calendar.getInstance().getTimeInMillis();
		if (userDAO.getCount() == 0) {
			initialData();
		}

		ApplicationSettings settings = applicationSettingsService.get();
		log.info("Starting {} background threads",
				settings.getBackgroundThreads());
		for (int i = 0; i < settings.getBackgroundThreads(); i++) {
			Future<Void> thread = worker.start("Thread " + i);
			threads.add(thread);
		}

	}

	private void initialData() {
		log.info("Populating database with default values");
		applicationSettingsService.save(new ApplicationSettings());
		userService.register(ADMIN_NAME, "admin",
				Arrays.asList(Role.ADMIN, Role.USER));
	}

	public long getStartupTime() {
		return startupTime;
	}

	@PreDestroy
	private void shutdown() {
		for (Future<Void> future : threads) {
			future.cancel(true);
		}
	}

}
