package com.commafeed.backend.dao;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.model.UserSettings_;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

import org.hibernate.query.criteria.CriteriaDefinition;

@Singleton
public class UserSettingsDAO extends GenericDAO<UserSettings> {

    public UserSettingsDAO(EntityManager entityManager, CommaFeedConfiguration config) {
        super(entityManager, UserSettings.class, config);
    }

    public UserSettings findByUser(User user) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), UserSettings.class) {
                            {
                                var settings = from(UserSettings.class);
                                where(equal(settings.get(UserSettings_.user), user));
                            }
                        })
                .setMaxResults(1)
                .getSingleResultOrNull();
    }
}
