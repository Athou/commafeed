package com.commafeed;

import jakarta.enterprise.inject.spi.CDI;

import org.kohsuke.MetaInfServices;

import com.commafeed.backend.service.db.DatabaseStartupService;

import io.quarkus.liquibase.runtime.LiquibaseSchemaProvider;
import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;

/**
 * Resets database between tests
 */
@MetaInfServices
public class DatabaseReset implements QuarkusTestBeforeEachCallback {

	@Override
	public void beforeEach(QuarkusTestMethodContext context) {
		new LiquibaseSchemaProvider().resetAllDatabases();
		CDI.current().select(DatabaseStartupService.class).get().populateInitialData();
	}
}
