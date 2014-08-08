package com.commafeed.backend.service;

import java.util.ResourceBundle;

public class ApplicationPropertiesService {

	private ResourceBundle bundle;

	public ApplicationPropertiesService() {
		bundle = ResourceBundle.getBundle("application");
	}

	public String getDatasource() {
		return bundle.getString("datasource");
	}

	public String getVersion() {
		return bundle.getString("version");
	}

	public String getGitCommit() {
		return bundle.getString("git.commit");
	}

	public boolean isProduction() {
		return Boolean.valueOf(bundle.getString("production"));
	}
}
