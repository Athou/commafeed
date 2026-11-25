package com.commafeed.backend.dao;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.model.UserRole_;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.JoinType;

import org.hibernate.query.criteria.CriteriaDefinition;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class UserRoleDAO extends GenericDAO<UserRole> {

    public UserRoleDAO(EntityManager entityManager, CommaFeedConfiguration config) {
        super(entityManager, UserRole.class, config);
    }

    public List<UserRole> findAll() {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), UserRole.class) {
                            {
                                var role = from(UserRole.class);
                                role.fetch(UserRole_.user, JoinType.LEFT);
                                distinct(true);
                            }
                        })
                .getResultList();
    }

    public List<UserRole> findAll(User user) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), UserRole.class) {
                            {
                                var role = from(UserRole.class);
                                where(equal(role.get(UserRole_.user), user));
                                distinct(true);
                            }
                        })
                .getResultList();
    }

    public Set<Role> findRoles(User user) {
        return findAll(user).stream().map(UserRole::getRole).collect(Collectors.toSet());
    }

    public long countAdmins() {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), Long.class) {
                            {
                                var role = from(UserRole.class);
                                select(count(role));
                                where(equal(role.get(UserRole_.role), Role.ADMIN));
                            }
                        })
                .getSingleResult();
    }
}
