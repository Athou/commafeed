package com.commafeed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.codahale.metrics.MetricRegistry;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;

public class CommaFeedDropwizardAppExtension extends DropwizardAppExtension<CommaFeedConfiguration> {

	public CommaFeedDropwizardAppExtension() {
		super(CommaFeedApplication.class, ResourceHelpers.resourceFilePath("config.test.yml"));
	}

	@Override
	public void after() {
		super.after();

		// clean database after each test
		DataSource dataSource = getConfiguration().getDataSourceFactory().build(new MetricRegistry(), "cleanup");
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement("DROP ALL OBJECTS")) {
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("could not cleanup database", e);
		}
	}

}
