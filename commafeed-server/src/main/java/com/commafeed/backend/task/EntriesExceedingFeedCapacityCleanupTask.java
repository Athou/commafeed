package com.commafeed.backend.task;

import java.util.concurrent.TimeUnit;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.service.db.DatabaseCleaningService;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Singleton
public class EntriesExceedingFeedCapacityCleanupTask extends ScheduledTask {

	private final CommaFeedConfiguration config;
	private final DatabaseCleaningService cleaner;

	@Override
	public void run() {
		int maxFeedCapacity = config.database().cleanup().maxFeedCapacity();
		if (maxFeedCapacity > 0) {
			cleaner.cleanEntriesForFeedsExceedingCapacity(maxFeedCapacity);
		}
	}

	@Override
	public long getInitialDelay() {
		return 10;
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
