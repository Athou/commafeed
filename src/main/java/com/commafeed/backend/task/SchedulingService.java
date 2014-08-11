package com.commafeed.backend.task;

import io.dropwizard.lifecycle.Managed;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Lists;

@Slf4j
public class SchedulingService implements Managed {

	public static interface ScheduledTask {
		void run();

		long getInitialDelay();

		long getPeriod();

		TimeUnit getTimeUnit();
	}

	private List<ScheduledTask> tasks = Lists.newArrayList();
	private ScheduledExecutorService executor;

	@Override
	public void start() throws Exception {
		executor = Executors.newScheduledThreadPool(tasks.size());
		for (final ScheduledTask task : tasks) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						task.run();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			};
			executor.scheduleWithFixedDelay(runnable, task.getInitialDelay(), task.getPeriod(), task.getTimeUnit());
		}
	}

	@Override
	public void stop() throws Exception {
		executor.shutdown();
	}

	public void register(ScheduledTask task) {
		tasks.add(task);
	}
}
