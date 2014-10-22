package com.commafeed.backend.task;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ScheduledTask {
	protected abstract void run();

	protected abstract long getInitialDelay();

	protected abstract long getPeriod();

	protected abstract TimeUnit getTimeUnit();

	public void register(ScheduledExecutorService executor) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					ScheduledTask.this.run();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		};
		executor.scheduleWithFixedDelay(runnable, getInitialDelay(), getPeriod(), getTimeUnit());
	}

}
