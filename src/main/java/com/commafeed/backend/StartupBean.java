package com.commafeed.backend;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
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
import com.google.api.client.util.Maps;

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
	Instance<FeedRefreshWorker> workers;

	private long startupTime;
	private Map<String, String> supportedLanguages = Maps.newHashMap();

	private ExecutorService executor;
	private MutableBoolean running = new MutableBoolean(true);

	@PostConstruct
	private void init() {

		startupTime = Calendar.getInstance().getTimeInMillis();
		if (userDAO.getCount() == 0) {
			initialData();
		}
		applicationSettingsService.applyLogLevel();

		initSupportedLanguages();

		ApplicationSettings settings = applicationSettingsService.get();
		int threads = settings.getBackgroundThreads();
		log.info("Starting {} background threads", threads);

		executor = Executors.newFixedThreadPool(Math.max(threads, 1));
		for (int i = 0; i < threads; i++) {
			final int threadId = i;
			executor.execute(new Runnable() {
				@Override
				public void run() {
					FeedRefreshWorker worker = workers.get();
					worker.start(running, "Thread " + threadId);
				}
			});
		}

	}

	private void initSupportedLanguages() {
		Properties props = new Properties();
		InputStream is = null;
		try {
			is = getClass().getResourceAsStream("/i18n/languages.properties");
			props.load(new InputStreamReader(is, "UTF-8"));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(is);
		}
		for (Object key : props.keySet()) {
			supportedLanguages.put(key.toString(),
					props.getProperty(key.toString()));
		}
	}

	private void initialData() {
		log.info("Populating database with default values");

		ApplicationSettings settings = new ApplicationSettings();
		settings.setAnnouncement("Set the Public URL in the admin section !");
		applicationSettingsService.save(settings);

		userService.register(USERNAME_ADMIN, "admin",
				Arrays.asList(Role.ADMIN, Role.USER));
		userService.register(USERNAME_DEMO, "demo", Arrays.asList(Role.USER));
	}

	public long getStartupTime() {
		return startupTime;
	}

	public Map<String, String> getSupportedLanguages() {
		return supportedLanguages;
	}

	@PreDestroy
	public void shutdown() {
		running.setValue(false);
		executor.shutdownNow();
		while (!executor.isTerminated()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("interrupted while waiting for threads to finish.");
			}
		}
	}

}
