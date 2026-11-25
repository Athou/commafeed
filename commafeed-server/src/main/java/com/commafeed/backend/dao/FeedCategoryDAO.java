package com.commafeed.backend.dao;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedCategory_;
import com.commafeed.backend.model.User;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;

import java.util.List;
import java.util.Objects;

@Singleton
public class FeedCategoryDAO extends GenericDAO<FeedCategory> {

    public FeedCategoryDAO(EntityManager entityManager, CommaFeedConfiguration config) {
        super(entityManager, FeedCategory.class, config);
    }

    public List<FeedCategory> findAll(User user) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), FeedCategory.class) {
                            {
                                var category = from(FeedCategory.class);
                                category.fetch(FeedCategory_.user, JoinType.INNER);
                                where(equal(category.get(FeedCategory_.user), user));
                            }
                        })
                .getResultList();
    }

    public FeedCategory findById(User user, Long id) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), FeedCategory.class) {
                            {
                                var category = from(FeedCategory.class);
                                where(
                                        equal(category.get(FeedCategory_.user), user),
                                        equal(category.get(FeedCategory_.id), id));
                            }
                        })
                .getSingleResultOrNull();
    }

    public FeedCategory findByName(User user, String name, FeedCategory parent) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), FeedCategory.class) {
                            {
                                var category = from(FeedCategory.class);
                                where(
                                        equal(category.get(FeedCategory_.user), user),
                                        equal(category.get(FeedCategory_.name), name),
                                        parentPredicate(this, category, parent));
                            }
                        })
                .getSingleResultOrNull();
    }

    public List<FeedCategory> findByParent(User user, FeedCategory parent) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), FeedCategory.class) {
                            {
                                var category = from(FeedCategory.class);
                                where(
                                        equal(category.get(FeedCategory_.user), user),
                                        parentPredicate(this, category, parent));
                            }
                        })
                .getResultList();
    }

    private static Predicate parentPredicate(
            HibernateCriteriaBuilder cb, Path<FeedCategory> category, FeedCategory parent) {
        Path<FeedCategory> parentPath = category.get(FeedCategory_.parent);
        return parent == null ? cb.isNull(parentPath) : cb.equal(parentPath, parent);
    }

    public List<FeedCategory> findAllChildrenCategories(User user, FeedCategory parent) {
        return findAll(user).stream().filter(c -> isChild(c, parent)).toList();
    }

    private boolean isChild(FeedCategory child, FeedCategory parent) {
        if (parent == null) {
            return true;
        }
        boolean isChild = false;
        while (child != null) {
            if (Objects.equals(child.getId(), parent.getId())) {
                isChild = true;
                break;
            }
            child = child.getParent();
        }
        return isChild;
    }
}
