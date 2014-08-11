package com.commafeed.backend.task;

import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

import com.commafeed.backend.service.DatabaseCleaningService;
import com.commafeed.backend.task.SchedulingService.ScheduledTask;

@RequiredArgsConstructor
public class OrphansCleanupTask implements ScheduledTask {

	private final DatabaseCleaningService cleaner;

	@Override
	public void run() {
		cleaner.cleanEntriesWithoutSubscriptions();
		cleaner.cleanFeedsWithoutSubscriptions();
		cleaner.cleanContentsWithoutEntries();
	}

	@Override
	public long getInitialDelay() {
		return 30;
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
