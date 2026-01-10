package com.commafeed;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;

import com.commafeed.backend.feed.FeedRefreshEngine;
import com.commafeed.backend.task.TaskScheduler;
import com.commafeed.security.password.PasswordConstraintValidator;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class CommaFeedApplication {

	private final FeedRefreshEngine feedRefreshEngine;
	private final TaskScheduler taskScheduler;
	private final CommaFeedConfiguration config;

	public void start(@Observes StartupEvent ev) {
		PasswordConstraintValidator.setStrict(config.users().strictPasswordPolicy());

		feedRefreshEngine.start();
		taskScheduler.start();
	}

	public void stop(@Observes ShutdownEvent ev) {
		feedRefreshEngine.stop();
		taskScheduler.stop();
	}

}
