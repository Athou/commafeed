package com.commafeed.backend.service;

import io.dropwizard.lifecycle.Managed;

import java.sql.Connection;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

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

import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.internal.SessionFactoryImpl;

import com.commafeed.CommaFeedApplication;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.UserRole.Role;

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
		new UnitOfWork<Void>(sessionFactory) {
			@Override
			protected Void runInSession() throws Exception {
				if (userDAO.count() == 0) {
					initialData();
				}
				return null;
			}
		}.run();
	}

	private void updateSchema() {
		try {
			Connection connection = null;
			try {
				Thread currentThread = Thread.currentThread();
				ClassLoader classLoader = currentThread.getContextClassLoader();
				ResourceAccessor accessor = new ClassLoaderResourceAccessor(classLoader);

				DataSource dataSource = getDataSource(sessionFactory);
				connection = dataSource.getConnection();
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

				Liquibase liq = new Liquibase("migrations.xml", accessor, database);
				liq.update("prod");
			} finally {
				if (connection != null) {
					connection.close();
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void initialData() {
		log.info("Populating database with default values");
		try {
			userService.register(CommaFeedApplication.USERNAME_ADMIN, "admin", "admin@commafeed.com", Arrays.asList(Role.ADMIN, Role.USER),
					true);
			if (config.getApplicationSettings().isCreateDemoAccount()) {
				userService.register(CommaFeedApplication.USERNAME_DEMO, "demo", "demo@commafeed.com", Arrays.asList(Role.USER), true);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void stop() throws Exception {

	}

	private static DataSource getDataSource(SessionFactory sessionFactory) {
		if (sessionFactory instanceof SessionFactoryImpl) {
			ConnectionProvider cp = ((SessionFactoryImpl) sessionFactory).getConnectionProvider();
			if (cp instanceof DatasourceConnectionProviderImpl) {
				return ((DatasourceConnectionProviderImpl) cp).getDataSource();
			}
		}
		return null;
	}

}
