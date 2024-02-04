package com.commafeed.backend.service.db;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.service.UserService;

import io.dropwizard.lifecycle.Managed;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import liquibase.Scope;
import liquibase.UpdateSummaryEnum;
import liquibase.changelog.ChangeLogParameters;
import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.command.core.helpers.ShowSummaryArgument;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.structure.DatabaseObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class DatabaseStartupService implements Managed {

	private final UnitOfWork unitOfWork;
	private final SessionFactory sessionFactory;
	private final UserDAO userDAO;
	private final UserService userService;
	private final CommaFeedConfiguration config;

	@Override
	public void start() {
		updateSchema();
		long count = unitOfWork.call(userDAO::count);
		if (count == 0) {
			unitOfWork.run(this::initialData);
		}
	}

	private void updateSchema() {
		log.info("checking if database schema needs updating");

		try (Session session = sessionFactory.openSession()) {
			session.doWork(connection -> {
				try {
					JdbcConnection jdbcConnection = new JdbcConnection(connection);
					Database database = getDatabase(jdbcConnection);

					Map<String, Object> scopeObjects = new HashMap<>();
					scopeObjects.put(Scope.Attr.database.name(), database);
					scopeObjects.put(Scope.Attr.resourceAccessor.name(),
							new ClassLoaderResourceAccessor(Thread.currentThread().getContextClassLoader()));

					Scope.child(scopeObjects, () -> {
						CommandScope command = new CommandScope(UpdateCommandStep.COMMAND_NAME);
						command.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
						command.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "migrations.xml");
						command.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, new ChangeLogParameters(database));
						command.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.OFF);
						command.execute();
					});

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		}

		log.info("database schema is up to date");
	}

	private Database getDatabase(JdbcConnection connection) throws DatabaseException {
		Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
		if (database instanceof PostgresDatabase) {
			database = new PostgresDatabase() {
				@Override
				public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
					return objectName;
				}
			};
			database.setConnection(connection);
		}

		return database;
	}

	private void initialData() {
		log.info("populating database with default values");
		try {
			userService.createAdminUser();
			if (config.getApplicationSettings().getCreateDemoAccount()) {
				userService.createDemoUser();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
