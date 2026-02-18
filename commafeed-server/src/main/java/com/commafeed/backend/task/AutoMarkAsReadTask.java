package com.commafeed.backend.task;

import java.util.concurrent.TimeUnit;

import jakarta.inject.Singleton;

import com.commafeed.backend.service.db.DatabaseCleaningService;

import lombok.RequiredArgsConstructor;

/**
 * Periodically marks entries as read if they have expired. Part of the auto-mark-read feature.
 */
@RequiredArgsConstructor
@Singleton
public class AutoMarkAsReadTask extends ScheduledTask {

	private final DatabaseCleaningService cleaner;

	@Override
	public void run() {
		cleaner.cleanExpiredAutoMarkAsReadStatuses();
	}

	@Override
	public long getInitialDelay() {
		return 5;
	}

	@Override
	public long getPeriod() {
		return 60;
	}

	@Override
	public TimeUnit getTimeUnit() {
		return TimeUnit.MINUTES;
	}

}
