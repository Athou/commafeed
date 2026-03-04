package com.commafeed;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.EntityManager;

import org.hibernate.Session;
import org.kohsuke.MetaInfServices;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;

/**
 * Resets database between tests
 */
@MetaInfServices
public class DatabaseReset implements QuarkusTestBeforeEachCallback {

	@Override
	public void beforeEach(QuarkusTestMethodContext context) {
		// stop the application to make sure that there are no active transactions when we truncate the tables
		getBean(CommaFeedApplication.class).stop(new ShutdownEvent());

		// truncate all tables so that we have a clean slate for the next test
		getBean(EntityManager.class).unwrap(Session.class).getSessionFactory().getSchemaManager().truncateMappedObjects();

		// restart the application
		getBean(CommaFeedApplication.class).start(new StartupEvent());
	}

	private static <T> T getBean(Class<T> clazz) {
		return CDI.current().select(clazz).get();
	}
}
