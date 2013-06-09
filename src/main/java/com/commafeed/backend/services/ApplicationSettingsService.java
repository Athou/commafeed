package com.commafeed.backend.services;

import java.util.Enumeration;

import javax.ejb.Singleton;
import javax.inject.Inject;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.commafeed.backend.dao.ApplicationSettingsDAO;
import com.commafeed.backend.model.ApplicationSettings;
import com.google.common.collect.Iterables;

@Singleton
public class ApplicationSettingsService {

	@Inject
	ApplicationSettingsDAO applicationSettingsDAO;

	private ApplicationSettings settings;

	public void save(ApplicationSettings settings) {
		this.settings = settings;
		applicationSettingsDAO.saveOrUpdate(settings);
		applyLogLevel();
	}

	public ApplicationSettings get() {
		if (settings == null) {
			settings = Iterables.getFirst(applicationSettingsDAO.findAll(),
					null);
		}
		return settings;
	}

	@SuppressWarnings("unchecked")
	public void applyLogLevel() {
		String logLevel = get().getLogLevel();
		Level level = Level.toLevel(logLevel);

		Enumeration<Logger> loggers = LogManager.getCurrentLoggers();
		while (loggers.hasMoreElements()) {
			Logger logger = loggers.nextElement();
			if (logger.getName().startsWith("com.commafeed")) {
				logger.setLevel(level);
			}
		}
	}
}
