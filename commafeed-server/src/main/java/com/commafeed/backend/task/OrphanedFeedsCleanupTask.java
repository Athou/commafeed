package com.commafeed.backend.task;

import java.util.concurrent.TimeUnit;

import jakarta.inject.Singleton;

import com.commafeed.backend.service.db.DatabaseCleaningService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Singleton
public class OrphanedFeedsCleanupTask extends ScheduledTask {

	private final DatabaseCleaningService cleaner;

	@Override
	public void run() {
		cleaner.cleanFeedsWithoutSubscriptions();
	}

	@Override
	public long getInitialDelay() {
		return 20;
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
