package com.commafeed.backend.services;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
public class CleaningService {

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@PersistenceContext
	EntityManager em;

//	@Schedule(hour = "*")
	protected void cleanOldStatuses() {
		int keepStatusDays = applicationSettingsService.get()
				.getKeepStatusDays();
		if (keepStatusDays > 0) {
			Query query = em.createNamedQuery("Statuses.deleteOld");
			query.executeUpdate();
		}
	}
}
