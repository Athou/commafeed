package com.commafeed.backend;

import java.util.Date;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.commafeed.backend.services.ApplicationSettingsService;

/**
 * Contains all scheduled tasks
 * 
 */
@Stateless
public class ScheduledTasks {

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Inject
	DatabaseCleaner cleaner;

	@PersistenceContext
	EntityManager em;

	/**
	 * clean old read statuses, runs every day at midnight
	 */
	@Schedule(hour = "0", persistent = false)
	private void cleanupOldStatuses() {
		Date threshold = applicationSettingsService.getUnreadThreshold();
		if (threshold != null) {
			cleaner.cleanStatusesOlderThan(threshold);
		}
	}
}
