package com.commafeed.backend.task;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import jakarta.inject.Singleton;

import io.quarkus.arc.All;

@Singleton
public class TaskScheduler {

	private final List<ScheduledTask> tasks;
	private final ScheduledExecutorService executor;

	public TaskScheduler(@All List<ScheduledTask> tasks) {
		this.tasks = tasks;
		this.executor = Executors.newScheduledThreadPool(tasks.size());
	}

	public void start() {
		tasks.forEach(task -> task.register(executor));
	}

	public void stop() {
		executor.shutdownNow();
	}
}
