package com.commafeed.backend.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserDAO;

import io.dropwizard.lifecycle.Managed;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.structure.DatabaseObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class StartupService implements Managed {

	private final SessionFactory sessionFactory;
	private final UserDAO userDAO;
	private final UserService userService;
	private final CommaFeedConfiguration config;

	@Override
	public void start() throws Exception {
		updateSchema();
		long count = UnitOfWork.call(sessionFactory, () -> userDAO.count());
		if (count == 0) {
			UnitOfWork.run(sessionFactory, this::initialData);
		}
	}

	private void updateSchema() {
		Session session = sessionFactory.openSession();
		session.doWork(connection -> {
			try {
				JdbcConnection jdbcConnection = new JdbcConnection(connection);
				Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);

				if (database instanceof PostgresDatabase) {
					database = new PostgresDatabase() {
						@Override
						public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
							return objectName;
						}
					};
					database.setConnection(jdbcConnection);
				}

				ResourceAccessor accessor = new ClassLoaderResourceAccessor(Thread.currentThread().getContextClassLoader());
				try (Liquibase liq = new Liquibase("migrations.xml", accessor, database)) {
					liq.update("prod");
				}

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		session.close();
	}

	private void initialData() {
		log.info("Populating database with default values");
		try {
			userService.createAdminUser();
			if (config.getApplicationSettings().getCreateDemoAccount()) {
				userService.createDemoUser();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void stop() throws Exception {

	}
}
