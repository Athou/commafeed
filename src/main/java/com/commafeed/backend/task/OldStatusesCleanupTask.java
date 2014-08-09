package com.commafeed.backend.task;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

import org.hibernate.SessionFactory;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.service.DatabaseCleaningService;
import com.commafeed.backend.task.SchedulingService.ScheduledTask;

@RequiredArgsConstructor
public class OldStatusesCleanupTask implements ScheduledTask {

	private final SessionFactory sessionFactory;
	private final CommaFeedConfiguration config;
	private final DatabaseCleaningService cleaner;

	@Override
	public void run() {
		new UnitOfWork<Void>(sessionFactory) {
			@Override
			protected Void runInSession() throws Exception {
				Date threshold = config.getApplicationSettings().getUnreadThreshold();
				if (threshold != null) {
					cleaner.cleanStatusesOlderThan(threshold);
				}
				return null;
			}
		}.run();
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
