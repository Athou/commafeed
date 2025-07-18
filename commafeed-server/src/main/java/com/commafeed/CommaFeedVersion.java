package com.commafeed;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import jakarta.inject.Singleton;

import lombok.Getter;

@Singleton
@Getter
public class CommaFeedVersion {

	private final String version;
	private final String gitCommit;

	public CommaFeedVersion() throws IOException {
		Properties properties = new Properties();
		try (InputStream stream = getClass().getResourceAsStream("/git.properties")) {
			if (stream != null) {
				properties.load(stream);
			}
		}

		this.version = properties.getProperty("git.build.version", "unknown");
		this.gitCommit = properties.getProperty("git.commit.id.abbrev", "unknown");
	}

}
