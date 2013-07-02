package com.commafeed.backend;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.feeds.FeedRefreshTaskGiver;
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
	DatabaseUpdater databaseUpdater;

	@Inject
	UserDAO userDAO;

	@Inject
	UserService userService;

	@Inject
	FeedRefreshTaskGiver taskGiver;

	@Inject
	ApplicationSettingsService applicationSettingsService;

	private long startupTime;
	private Map<String, String> supportedLanguages = Maps.newHashMap();

	@PostConstruct
	private void init() {

		startupTime = System.currentTimeMillis();
		databaseUpdater.update();

		if (userDAO.getCount() == 0) {
			initialData();
		}
		applicationSettingsService.applyLogLevel();

		initSupportedLanguages();
		taskGiver.start();
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
		settings.setAnnouncement("Set the Public URL in the admin section!");
		applicationSettingsService.save(settings);

		try {
			userService.register(USERNAME_ADMIN, "admin",
					"admin@commafeed.com",
					Arrays.asList(Role.ADMIN, Role.USER), true);
			userService.register(USERNAME_DEMO, "demo", "demo@commafeed.com",
					Arrays.asList(Role.USER), true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public long getStartupTime() {
		return startupTime;
	}

	public Map<String, String> getSupportedLanguages() {
		return supportedLanguages;
	}
}
