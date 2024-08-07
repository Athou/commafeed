package com.commafeed.backend.task;

import java.util.concurrent.TimeUnit;

import com.commafeed.backend.service.db.DatabaseCleaningService;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Singleton
public class OrphanedContentsCleanupTask extends ScheduledTask {

	private final DatabaseCleaningService cleaner;

	@Override
	public void run() {
		cleaner.cleanContentsWithoutEntries();
	}

	@Override
	public long getInitialDelay() {
		return 25;
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
