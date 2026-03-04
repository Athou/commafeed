package com.commafeed.backend.task;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import jakarta.inject.Singleton;

import com.commafeed.CommaFeedConfiguration;
import com.google.common.util.concurrent.MoreExecutors;

import io.quarkus.arc.All;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class TaskScheduler {

	private final List<ScheduledTask> tasks;
	private final CommaFeedConfiguration config;
	private final ScheduledExecutorService executor;

	public TaskScheduler(@All List<ScheduledTask> tasks, CommaFeedConfiguration config) {
		this.tasks = tasks;
		this.config = config;
		this.executor = Executors.newScheduledThreadPool(tasks.size());
	}

	public void start() {
		tasks.forEach(task -> task.register(executor));
	}

	public void stop() {
		MoreExecutors.shutdownAndAwaitTermination(executor, config.shutdownTimeout());
	}
}
