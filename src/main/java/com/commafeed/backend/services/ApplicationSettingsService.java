package com.commafeed.backend.services;

import javax.ejb.Singleton;
import javax.inject.Inject;

import com.commafeed.backend.dao.ApplicationSettingsDAO;
import com.commafeed.backend.model.ApplicationSettings;
import com.google.common.collect.Iterables;

@Singleton
public class ApplicationSettingsService {

	@Inject
	ApplicationSettingsDAO applicationSettingsDAO;

	private ApplicationSettings settings;

	public void save(ApplicationSettings settings) {
		this.settings = settings;
		applicationSettingsDAO.saveOrUpdate(settings);
	}

	public ApplicationSettings get() {
		if (settings == null) {
			settings = Iterables.getFirst(applicationSettingsDAO.findAll(),
					null);
		}
		return settings;
	}

}
