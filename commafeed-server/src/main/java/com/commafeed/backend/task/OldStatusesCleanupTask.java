package com.commafeed.backend.task;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.service.db.DatabaseCleaningService;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Singleton
public class OldStatusesCleanupTask extends ScheduledTask {

	private final CommaFeedConfiguration config;
	private final DatabaseCleaningService cleaner;

	@Override
	public void run() {
		Instant threshold = config.database().cleanup().statusesInstantThreshold();
		if (threshold != null) {
			cleaner.cleanStatusesOlderThan(threshold);
		}
	}

	@Override
	public long getInitialDelay() {
		return 15;
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
