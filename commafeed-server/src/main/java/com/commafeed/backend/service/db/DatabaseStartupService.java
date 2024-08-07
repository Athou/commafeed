package com.commafeed.backend.service.db;

import javax.sql.DataSource;

import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.service.UserService;
import com.commafeed.config.CommaFeedConfiguration;

import io.quarkus.liquibase.LiquibaseFactory;
import jakarta.inject.Singleton;
import liquibase.Liquibase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.structure.DatabaseObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Singleton
public class DatabaseStartupService {

	private final LiquibaseFactory liquibaseFactory;
	private final DataSource dataSource;
	private final UnitOfWork unitOfWork;
	private final UserDAO userDAO;
	private final UserService userService;
	private final CommaFeedConfiguration config;

	public void migrateDatabase() {
		try (Liquibase liquibase = liquibaseFactory.createLiquibase()) {
			if (liquibase.getDatabase() instanceof PostgresDatabase) {
				PostgresDatabase postgresDatabase = new PostgresDatabase() {
					@Override
					public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
						return objectName;
					}
				};
				postgresDatabase.setConnection(new JdbcConnection(dataSource.getConnection()));

				try (Liquibase postgresLiquibase = new Liquibase(liquibase.getChangeLogFile(), liquibase.getResourceAccessor(),
						postgresDatabase)) {
					migrate(postgresLiquibase);
				}
			} else {
				migrate(liquibase);
			}
		} catch (Exception e) {
			throw new RuntimeException("failed to migrate database", e);
		}
	}

	@SuppressWarnings("deprecation")
	private void migrate(Liquibase liquibase) throws LiquibaseException {
		liquibase.update(liquibaseFactory.createContexts(), liquibaseFactory.createLabels());
	}

	public void populateInitialData() {
		long count = unitOfWork.call(userDAO::count);
		if (count == 0) {
			unitOfWork.run(this::initialData);
		}
	}

	private void initialData() {
		log.info("populating database with default values");
		try {
			userService.createAdminUser();
			if (config.createDemoAccount()) {
				userService.createDemoUser();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
