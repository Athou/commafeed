package com.commafeed.backend.task;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;

import com.commafeed.backend.service.DatabaseCleaningService;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class OrphanedFeedsCleanupTask extends ScheduledTask {

	private final DatabaseCleaningService cleaner;

	@Override
	public void run() {
		cleaner.cleanFeedsWithoutSubscriptions();
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
