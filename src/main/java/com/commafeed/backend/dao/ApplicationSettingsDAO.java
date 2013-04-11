package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.commafeed.backend.model.ApplicationSettings;
import com.google.common.collect.Iterables;
import com.uaihebert.factory.EasyCriteriaFactory;
import com.uaihebert.model.EasyCriteria;

@Stateless
public class ApplicationSettingsDAO {

	@PersistenceContext
	protected EntityManager em;

	public void save(ApplicationSettings settings) {
		if (settings.getId() == null) {
			em.persist(settings);
		} else {
			em.merge(settings);
		}
	}

	public ApplicationSettings get() {
		EasyCriteria<ApplicationSettings> criteria = EasyCriteriaFactory
				.createQueryCriteria(em, ApplicationSettings.class);
		List<ApplicationSettings> list = criteria.getResultList();
		return Iterables.getFirst(list, null);
	}
}
