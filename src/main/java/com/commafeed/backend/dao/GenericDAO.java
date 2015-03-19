package com.commafeed.backend.dao;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.Collection;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.AbstractModel;
import com.mysema.query.jpa.hibernate.HibernateQuery;

public abstract class GenericDAO<T extends AbstractModel> extends AbstractDAO<T> {

	protected GenericDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	protected HibernateQuery newQuery() {
		return new HibernateQuery(currentSession());
	}

	public void saveOrUpdate(T model) {
		persist(model);
	}

	public void saveOrUpdate(Collection<T> models) {
		models.forEach(m -> persist(m));
	}

	public void update(T model) {
		currentSession().update(model);
	}

	public void update(Collection<T> models) {
		models.forEach(m -> update(m));
	}

	public void merge(T model) {
		currentSession().merge(model);
	}

	public void merge(Collection<T> models) {
		models.forEach(m -> merge(m));
	}

	public T findById(Long id) {
		return get(id);
	}

	public void delete(T object) {
		if (object != null) {
			currentSession().delete(object);
		}
	}

	public int delete(Collection<T> objects) {
		objects.forEach(o -> delete(o));
		return objects.size();
	}

}
