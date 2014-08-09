package com.commafeed.backend.task;

import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

import org.hibernate.SessionFactory;

import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.service.DatabaseCleaningService;
import com.commafeed.backend.task.SchedulingService.ScheduledTask;

@RequiredArgsConstructor
public class OrphansCleanupTask implements ScheduledTask {

	private final SessionFactory sessionFactory;
	private final DatabaseCleaningService cleaner;

	@Override
	public void run() {
		new UnitOfWork<Void>(sessionFactory) {
			@Override
			protected Void runInSession() throws Exception {
				cleaner.cleanEntriesWithoutSubscriptions();
				cleaner.cleanFeedsWithoutSubscriptions();
				cleaner.cleanContentsWithoutEntries();
				return null;
			}
		}.run();
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
