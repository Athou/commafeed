package com.commafeed.backend.dao;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.User_;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.CriteriaDefinition;

@Singleton
public class UserDAO extends GenericDAO<User> {

    public UserDAO(EntityManager entityManager, CommaFeedConfiguration config) {
        super(entityManager, User.class, config);
    }

    public User findByName(String name) {
        return findByIgnoreCase(User_.name, name);
    }

    public User findByApiKey(String key) {
        // api keys are generated as lowercase hex, compare exactly with the lowercased input so the index can be used
        return query(
                        new CriteriaDefinition<>(getEntityManager(), User.class) {
                            {
                                var user = from(User.class);
                                where(equal(user.get(User_.apiKey), StringUtils.lowerCase(key)));
                            }
                        })
                .getSingleResultOrNull();
    }

    public User findByEmail(String email) {
        return findByIgnoreCase(User_.email, email);
    }

    private User findByIgnoreCase(SingularAttribute<User, String> attribute, String value) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), User.class) {
                            {
                                var user = from(User.class);
                                where(equalsIgnoreCase(this, user.get(attribute), value));
                            }
                        })
                .getSingleResultOrNull();
    }

    public long count() {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), Long.class) {
                            {
                                select(count(from(User.class)));
                            }
                        })
                .getSingleResult();
    }
}
