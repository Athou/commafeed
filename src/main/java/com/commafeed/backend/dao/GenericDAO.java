package com.commafeed.backend.dao;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;

import org.hibernate.Query;
import org.hibernate.Session;

import com.commafeed.backend.model.AbstractModel;
import com.google.common.reflect.TypeToken;

@SuppressWarnings("serial")
public abstract class GenericDAO<T extends AbstractModel> {

	private TypeToken<T> type = new TypeToken<T>(getClass()) {
	};

	@PersistenceContext
	protected EntityManager em;

	protected CriteriaBuilder builder;

	@PostConstruct
	public void init() {
		builder = em.getCriteriaBuilder();
	}

	public void saveOrUpdate(Collection<? extends AbstractModel> models) {
		Session session = em.unwrap(Session.class);
		int i = 1;
		for (AbstractModel model : models) {
			session.saveOrUpdate(model);
			if (i % 50 == 0) {
				session.flush();
				session.clear();
			}
		}
	}

	public void saveOrUpdate(AbstractModel... models) {
		saveOrUpdate(Arrays.asList(models));
	}

	public void delete(AbstractModel object) {
		if (object != null) {
			object = em.merge(object);
			em.remove(object);
		}
	}

	public void delete(Collection<? extends AbstractModel> objects) {
		for (AbstractModel object : objects) {
			delete(object);
		}
	}

	public void deleteById(Long id) {
		Object ref = em.getReference(getType(), id);
		if (ref != null) {
			em.remove(ref);
		}
	}

	public T findById(Long id) {
		T t = em.find(getType(), id);
		return t;
	}

	public List<T> findAll() {
		CriteriaQuery<T> query = builder.createQuery(getType());
		query.from(getType());
		return em.createQuery(query).getResultList();
	}

	public List<T> findAll(int startIndex, int count) {
		CriteriaQuery<T> query = builder.createQuery(getType());
		query.from(getType());
		TypedQuery<T> q = em.createQuery(query);
		q.setMaxResults(count);
		q.setFirstResult(startIndex);
		return q.getResultList();
	}

	public List<T> findAll(int startIndex, int count, String orderBy,
			boolean asc) {

		CriteriaQuery<T> query = builder.createQuery(getType());
		Root<T> root = query.from(getType());

		if (asc) {
			query.orderBy(builder.asc(root.get(orderBy)));
		} else {
			query.orderBy(builder.desc(root.get(orderBy)));
		}

		TypedQuery<T> q = em.createQuery(query);
		q.setMaxResults(count);
		q.setFirstResult(startIndex);
		return q.getResultList();
	}

	public long getCount() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<T> root = query.from(getType());
		query.select(builder.count(root));
		return em.createQuery(query).getSingleResult();
	}

	protected <V> List<T> findByField(Attribute<T, V> field, V value) {
		return findByField(field, value, false);
	}

	protected <V> List<T> findByField(Attribute<T, V> field, V value,
			boolean cache) {
		CriteriaQuery<T> query = builder.createQuery(getType());
		Root<T> root = query.from(getType());

		query.where(builder.equal(root.get(field.getName()), value));
		TypedQuery<T> q = em.createQuery(query);
		if (cache) {
			cache(q);
		}
		return em.createQuery(query).getResultList();
	}

	protected <Q> void limit(TypedQuery<Q> query, int offset, int limit) {
		if (offset > -1) {
			query.setFirstResult(offset);
		}
		if (limit > -1) {
			query.setMaxResults(limit);
		}
	}

	protected <Q> TypedQuery<Q> readOnly(TypedQuery<Q> query) {
		query.unwrap(Query.class).setReadOnly(true);
		return query;
	}

	protected <Q> TypedQuery<Q> cache(TypedQuery<Q> query) {
		query.unwrap(Query.class).setCacheable(true);
		return query;
	}
	
	protected void setTimeout(javax.persistence.Query query, int queryTimeout) {
		if (queryTimeout > 0) {
			query.setHint("javax.persistence.query.timeout", queryTimeout);
		}
	}

	@SuppressWarnings("unchecked")
	protected Class<T> getType() {
		return (Class<T>) type.getRawType();
	}
}
