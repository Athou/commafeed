package com.commafeed.backend.task;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.service.DatabaseCleaningService;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class OldEntriesCleanupTask extends ScheduledTask {

	private final CommaFeedConfiguration config;
	private final DatabaseCleaningService cleaner;

	@Override
	public void run() {
		int maxFeedCapacity = config.getApplicationSettings().getMaxFeedCapacity();
		if (maxFeedCapacity > 0) {
			cleaner.cleanEntriesForFeedsExceedingCapacity(maxFeedCapacity);
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
