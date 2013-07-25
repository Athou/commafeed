package com.commafeed.backend;

import java.util.Date;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.services.ApplicationSettingsService;

@Stateless
public class ScheduledTasks {
	protected final static Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Inject
	DatabaseCleaner cleaner;

	@PersistenceContext
	EntityManager em;

	// every day at midnight
	@Schedule(hour = "0", persistent = false)
	private void cleanupOldStatuses() {
		Date threshold = applicationSettingsService.get().getUnreadThreshold();
		if (threshold != null) {
			cleaner.cleanStatusesOlderThan(threshold);
		}
	}
}
