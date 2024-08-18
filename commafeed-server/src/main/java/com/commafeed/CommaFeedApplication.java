package com.commafeed;

import com.commafeed.backend.feed.FeedRefreshEngine;
import com.commafeed.backend.service.db.DatabaseStartupService;
import com.commafeed.backend.task.TaskScheduler;
import com.commafeed.security.password.PasswordConstraintValidator;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class CommaFeedApplication {

	public static final String USERNAME_ADMIN = "admin";
	public static final String USERNAME_DEMO = "demo";

	private final DatabaseStartupService databaseStartupService;
	private final FeedRefreshEngine feedRefreshEngine;
	private final TaskScheduler taskScheduler;
	private final CommaFeedConfiguration config;

	public void start(@Observes StartupEvent ev) {
		PasswordConstraintValidator.setStrict(config.users().strictPasswordPolicy());

		databaseStartupService.populateInitialData();

		feedRefreshEngine.start();
		taskScheduler.start();
	}

	public void stop(@Observes ShutdownEvent ev) {
		feedRefreshEngine.stop();
		taskScheduler.stop();
	}

}
