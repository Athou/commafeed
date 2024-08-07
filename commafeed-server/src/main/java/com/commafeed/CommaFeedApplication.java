package com.commafeed;

import java.time.Instant;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.commafeed.backend.feed.FeedRefreshEngine;
import com.commafeed.backend.service.db.DatabaseStartupService;
import com.commafeed.backend.service.db.H2MigrationService;
import com.commafeed.backend.task.TaskScheduler;
import com.commafeed.config.CommaFeedConfiguration;
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

	public static final Instant STARTUP_TIME = Instant.now();

	@ConfigProperty(name = "quarkus.datasource.jdbc.url")
	String jdbcUrl;

	@ConfigProperty(name = "quarkus.datasource.username", defaultValue = "")
	String userName;

	@ConfigProperty(name = "quarkus.datasource.password", defaultValue = "")
	String password;

	private final H2MigrationService h2MigrationService;
	private final DatabaseStartupService databaseStartupService;
	private final FeedRefreshEngine feedRefreshEngine;
	private final TaskScheduler taskScheduler;
	private final CommaFeedConfiguration config;

	public void start(@Observes StartupEvent ev) {
		PasswordConstraintValidator.setStrict(config.strictPasswordPolicy());

		h2MigrationService.migrateIfNeeded(jdbcUrl, userName, password);
		databaseStartupService.migrateDatabase();
		databaseStartupService.populateInitialData();

		feedRefreshEngine.start();
		taskScheduler.start();
	}

	public void stop(@Observes ShutdownEvent ev) {
		feedRefreshEngine.stop();
		taskScheduler.stop();
	}

}
