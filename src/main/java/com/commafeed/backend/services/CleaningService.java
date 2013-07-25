package com.commafeed.backend.services;

import java.util.Date;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class CleaningService {

	protected final static Logger log = LoggerFactory.getLogger(CleaningService.class);

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@PersistenceContext
	EntityManager em;

	// every day at midnight
	@Schedule(hour = "0", persistent = false)
	private void cleanOldStatuses() {
		Date threshold = applicationSettingsService.get().getUnreadThreshold();
		if (threshold != null) {
			log.info("cleaning old statuses");
			Query query = em.createNamedQuery("Statuses.deleteOld");
			query.setParameter("date", threshold);
			query.executeUpdate();
		}
	}
}
