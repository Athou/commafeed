package com.commafeed.backend;

import java.sql.Connection;
import java.util.ResourceBundle;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class DatabaseUpdater {

	public void update() {
		String datasourceName = ResourceBundle.getBundle("application")
				.getString("datasource");
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

				Database database = DatabaseFactory.getInstance()
						.findCorrectDatabaseImplementation(
								new JdbcConnection(connection));

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
