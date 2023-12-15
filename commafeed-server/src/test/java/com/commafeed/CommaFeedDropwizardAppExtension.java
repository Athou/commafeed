package com.commafeed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import com.codahale.metrics.MetricRegistry;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;

public class CommaFeedDropwizardAppExtension extends DropwizardAppExtension<CommaFeedConfiguration> {

	private static final List<String> TABLES = Arrays.asList("FEEDENTRYSTATUSES", "FEEDENTRYTAGS", "FEEDENTRIES", "FEEDENTRYCONTENTS",
			"FEEDSUBSCRIPTIONS", "FEEDS", "FEEDCATEGORIES");

	public CommaFeedDropwizardAppExtension() {
		super(CommaFeedApplication.class, ResourceHelpers.resourceFilePath("config.test.yml"));
	}

	@Override
	public void after() {
		super.after();

		// clean database after each test
		DataSource dataSource = getConfiguration().getDataSourceFactory().build(new MetricRegistry(), "cleanup");
		try (Connection connection = dataSource.getConnection()) {
			for (String table : TABLES) {
				PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table);
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			throw new RuntimeException("could not cleanup database", e);
		}
	}

}
