package com.commafeed;

import org.kohsuke.MetaInfServices;

import com.commafeed.backend.service.db.DatabaseStartupService;

import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import jakarta.enterprise.inject.spi.CDI;

/**
 * Resets database between tests
 */
@MetaInfServices
public class DatabaseReset implements QuarkusTestBeforeEachCallback {

	@Override
	public void beforeEach(QuarkusTestMethodContext context) {
		DatabaseStartupService databaseStartupService = CDI.current().select(DatabaseStartupService.class).get();
		databaseStartupService.migrateDatabase();
		databaseStartupService.populateInitialData();
	}
}
