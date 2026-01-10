package com.commafeed;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.EntityManager;

import org.hibernate.Session;
import org.kohsuke.MetaInfServices;

import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;

/**
 * Resets database between tests
 */
@MetaInfServices
public class DatabaseReset implements QuarkusTestBeforeEachCallback {

	@Override
	public void beforeEach(QuarkusTestMethodContext context) {
		CDI.current()
				.select(EntityManager.class)
				.get()
				.unwrap(Session.class)
				.getSessionFactory()
				.getSchemaManager()
				.truncateMappedObjects();
	}
}
