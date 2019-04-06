package com.commafeed.backend.dao;

import java.util.Collection;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.AbstractModel;
import com.querydsl.jpa.impl.JPAQueryFactory;

import io.dropwizard.hibernate.AbstractDAO;

public abstract class GenericDAO<T extends AbstractModel> extends AbstractDAO<T> {

	private JPAQueryFactory factory;

	protected GenericDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
		this.factory = new JPAQueryFactory(() -> sessionFactory.createEntityManager());
	}

	protected JPAQueryFactory query() {
		return factory;
	}

	public void saveOrUpdate(T model) {
		persist(model);
	}

	public void saveOrUpdate(Collection<T> models) {
		models.forEach(m -> persist(m));
	}

	public void update(T model) {
		currentSession().merge(model);
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
