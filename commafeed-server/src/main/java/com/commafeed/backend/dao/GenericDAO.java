package com.commafeed.backend.dao;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.model.AbstractModel;
import com.commafeed.backend.model.AbstractModel_;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.SingularAttribute;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.hibernate.jpa.SpecHints;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;

import java.time.Duration;
import java.util.Collection;
import java.util.function.Function;

@RequiredArgsConstructor
public abstract class GenericDAO<T extends AbstractModel> {

    private static final char LIKE_ESCAPE_CHAR = '!';

    @Getter(lombok.AccessLevel.PROTECTED)
    private final EntityManager entityManager;

    private final Class<T> entityClass;
    private final CommaFeedConfiguration config;

    protected <R> TypedQuery<R> query(CriteriaQuery<R> criteria) {
        return withTimeout(entityManager.createQuery(criteria), config.database().queryTimeout());
    }

    @SuppressWarnings("unchecked")
    protected static <X, Y> Join<X, Y> fetchJoin(
            From<?, X> from, SingularAttribute<? super X, Y> attribute, JoinType joinType) {
        return (Join<X, Y>) from.fetch(attribute, joinType);
    }

    protected static Predicate containsIgnoreCase(
            HibernateCriteriaBuilder cb, Expression<String> expression, String value) {
        String escaped =
                value.replace(String.valueOf(LIKE_ESCAPE_CHAR), String.valueOf(LIKE_ESCAPE_CHAR) + LIKE_ESCAPE_CHAR)
                        .replace("%", LIKE_ESCAPE_CHAR + "%")
                        .replace("_", LIKE_ESCAPE_CHAR + "_");
        return cb.like(cb.lower(expression), cb.lower(cb.value("%" + escaped + "%")), LIKE_ESCAPE_CHAR);
    }

    protected static Predicate equalsIgnoreCase(
            HibernateCriteriaBuilder cb, Expression<String> expression, String value) {
        return cb.equal(cb.lower(expression), cb.lower(cb.value(value)));
    }

    public void persist(T model) {
        entityManager.persist(model);
    }

    public T merge(T model) {
        return entityManager.merge(model);
    }

    public T findById(Long id) {
        return entityManager.find(entityClass, id);
    }

    protected int updateByIds(Collection<Long> ids, Function<CriteriaUpdate<T>, CriteriaUpdate<T>> setter) {
        if (ids.isEmpty()) {
            return 0;
        }
        CriteriaUpdate<T> update = entityManager.getCriteriaBuilder().createCriteriaUpdate(entityClass);
        update.where(update.from(entityClass).get(AbstractModel_.id).in(ids));
        return entityManager.createQuery(setter.apply(update)).executeUpdate();
    }

    protected int deleteByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return 0;
        }
        CriteriaDelete<T> delete = entityManager.getCriteriaBuilder().createCriteriaDelete(entityClass);
        delete.where(delete.from(entityClass).get(AbstractModel_.id).in(ids));
        return entityManager.createQuery(delete).executeUpdate();
    }

    public void delete(T object) {
        if (object != null) {
            entityManager.remove(object);
        }
    }

    public int delete(Collection<T> objects) {
        objects.forEach(this::delete);
        return objects.size();
    }

    private <R> TypedQuery<R> withTimeout(TypedQuery<R> query, Duration timeout) {
        if (!timeout.isZero()) {
            query.setHint(SpecHints.HINT_SPEC_QUERY_TIMEOUT, Math.toIntExact(timeout.toMillis()));
        }
        return query;
    }
}
