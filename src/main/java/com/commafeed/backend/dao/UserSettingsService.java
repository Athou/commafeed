package com.commafeed.backend.dao;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.uaihebert.factory.EasyCriteriaFactory;
import com.uaihebert.model.EasyCriteria;

@Stateless
@SuppressWarnings("serial")
public class UserSettingsService extends GenericDAO<UserSettings, Long> {

	public UserSettings findByUser(User user) {

		EasyCriteria<UserSettings> criteria = EasyCriteriaFactory
				.createQueryCriteria(em, getType());
		criteria.andEquals(MF.i(proxy().getUser()), user);

		UserSettings settings = null;
		try {
			settings = criteria.getSingleResult();
		} catch (NoResultException e) {
			settings = null;
		}
		return settings;
	}
}
