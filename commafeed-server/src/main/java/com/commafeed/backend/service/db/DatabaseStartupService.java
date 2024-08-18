package com.commafeed.backend.service.db;

import org.kohsuke.MetaInfServices;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.service.UserService;

import jakarta.inject.Singleton;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.structure.DatabaseObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Singleton
public class DatabaseStartupService {

	private final UnitOfWork unitOfWork;
	private final UserDAO userDAO;
	private final UserService userService;
	private final CommaFeedConfiguration config;

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
			if (config.users().createDemoAccount()) {
				userService.createDemoUser();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Register a postgresql database in liquibase that doesn't escape columns, so that we can use lower case columns
	 */
	@MetaInfServices(Database.class)
	public static class LowerCaseColumnsPostgresDatabase extends PostgresDatabase {
		@Override
		public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
			return objectName;
		}

		@Override
		public int getPriority() {
			return super.getPriority() + 1;
		}
	}

}
