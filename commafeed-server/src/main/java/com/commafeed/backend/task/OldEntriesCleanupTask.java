package com.commafeed.backend.task;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.service.db.DatabaseCleaningService;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Singleton
public class OldEntriesCleanupTask extends ScheduledTask {

	private final CommaFeedConfiguration config;
	private final DatabaseCleaningService cleaner;

	@Override
	public void run() {
		Duration entriesMaxAge = config.database().cleanup().entriesMaxAge();
		if (!entriesMaxAge.isZero()) {
			Instant threshold = Instant.now().minus(entriesMaxAge);
			cleaner.cleanEntriesOlderThan(threshold);
		}
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
