package com.commafeed.backend.services;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.commafeed.backend.dao.ApplicationSettingsDAO;
import com.commafeed.backend.model.ApplicationSettings;
import com.google.common.collect.Iterables;

@Stateless
public class ApplicationSettingsService {

	@Inject
	ApplicationSettingsDAO applicationSettingsDAO;

	public void save(ApplicationSettings settings) {
		applicationSettingsDAO.saveOrUpdate(settings);
	}

	public ApplicationSettings get() {
		return Iterables.getFirst(applicationSettingsDAO.findAll(), null);
	}

}
