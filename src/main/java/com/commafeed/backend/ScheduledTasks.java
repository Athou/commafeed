package com.commafeed.backend;

import java.util.Date;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;

import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.backend.services.DatabaseCleaningService;

/**
 * Contains all scheduled tasks
 * 
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ScheduledTasks {

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Inject
	DatabaseCleaningService cleaner;

	/**
	 * clean old read statuses
	 */
	@Schedule(hour = "*", persistent = false)
	private void cleanupOldStatuses() {
		Date threshold = applicationSettingsService.getUnreadThreshold();
		if (threshold != null) {
			cleaner.cleanStatusesOlderThan(threshold);
		}
	}

	/**
	 * clean feeds without subscriptions, then clean contents without entries
	 */
	@Schedule(hour = "*", persistent = false)
	private void cleanFeedsAndContents() {
		cleaner.cleanEntriesWithoutSubscriptions();
		cleaner.cleanFeedsWithoutSubscriptions();
		cleaner.cleanContentsWithoutEntries();
	}
}
