package com.commafeed.backend;

import java.sql.Connection;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.structure.DatabaseObject;

import com.commafeed.backend.services.ApplicationPropertiesService;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class DatabaseUpdater {

	public void update() {
		ApplicationPropertiesService properties = ApplicationPropertiesService.get();
		String datasourceName = properties.getDatasource();
		try {
			Context context = null;
			Connection connection = null;
			try {
				Thread currentThread = Thread.currentThread();
				ClassLoader classLoader = currentThread.getContextClassLoader();
				ResourceAccessor accessor = new ClassLoaderResourceAccessor(
						classLoader);

				context = new InitialContext();
				DataSource dataSource = (DataSource) context
						.lookup(datasourceName);
				connection = dataSource.getConnection();
				JdbcConnection jdbcConnection = new JdbcConnection(connection);

				Database database = DatabaseFactory.getInstance()
						.findCorrectDatabaseImplementation(
								jdbcConnection);

				if (database instanceof PostgresDatabase) {
					database = new PostgresDatabase() {
						@Override
						public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
							return objectName;
						}
					};
					database.setConnection(jdbcConnection);
				}

				Liquibase liq = new Liquibase(
						"changelogs/db.changelog-master.xml", accessor,
						database);
				liq.update("prod");
			} finally {
				if (context != null) {
					context.close();
				}
				if (connection != null) {
					connection.close();
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
