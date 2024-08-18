package com.commafeed;

import org.kohsuke.MetaInfServices;

import com.commafeed.backend.service.db.DatabaseStartupService;

import io.quarkus.liquibase.LiquibaseFactory;
import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import jakarta.enterprise.inject.spi.CDI;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

/**
 * Resets database between tests
 */
@MetaInfServices
public class DatabaseReset implements QuarkusTestBeforeEachCallback {

	@SuppressWarnings("deprecation")
	@Override
	public void beforeEach(QuarkusTestMethodContext context) {
		LiquibaseFactory liquibaseFactory = CDI.current().select(LiquibaseFactory.class).get();
		try (Liquibase liquibase = liquibaseFactory.createLiquibase()) {
			liquibase.dropAll();
			liquibase.update(liquibaseFactory.createContexts(), liquibaseFactory.createLabels());
		} catch (LiquibaseException e) {
			throw new RuntimeException(e);
		}

		DatabaseStartupService databaseStartupService = CDI.current().select(DatabaseStartupService.class).get();
		databaseStartupService.populateInitialData();
	}
}
