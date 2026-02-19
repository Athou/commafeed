package com.commafeed.backend.service.db;

import jakarta.inject.Singleton;

import org.kohsuke.MetaInfServices;

import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserDAO;

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

	public boolean isInitialSetupRequired() {
		return unitOfWork.call(userDAO::count) == 0;
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
