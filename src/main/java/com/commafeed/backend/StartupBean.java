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

import org.apache.commons.lang.mutable.MutableBoolean;
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
	public static final String USERNAME_ADMIN = "admin";
	public static final String USERNAME_DEMO = "demo";

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

	private MutableBoolean running = new MutableBoolean(true);

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
			Future<Void> thread = worker.start(running, "Thread " + i);
			threads.add(thread);
		}

	}

	private void initialData() {
		log.info("Populating database with default values");
		applicationSettingsService.save(new ApplicationSettings());
		userService.register(USERNAME_ADMIN, "admin",
				Arrays.asList(Role.ADMIN, Role.USER));
		userService.register(USERNAME_DEMO, "demo", Arrays.asList(Role.USER));
	}

	public long getStartupTime() {
		return startupTime;
	}

	@PreDestroy
	public void shutdown() {
		running.setValue(false);
	}

}
