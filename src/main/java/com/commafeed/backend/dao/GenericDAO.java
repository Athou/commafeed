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

import org.hibernate.Session;
import org.hibernate.ejb.EntityManagerImpl;

import com.commafeed.backend.model.AbstractModel;
import com.google.common.reflect.TypeToken;
import com.uaihebert.factory.EasyCriteriaFactory;
import com.uaihebert.model.EasyCriteria;

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
		int i = 1;
		EntityManagerImpl impl = (EntityManagerImpl) em.getDelegate();
		Session session = impl.getSession();
		for (AbstractModel model : models) {
			session.saveOrUpdate(model);

			if (i % 20 == 0) {
				em.flush();
			}
			i++;
		}
	}

	public void saveOrUpdate(AbstractModel... models) {
		saveOrUpdate(Arrays.asList(models));
	}

	public void delete(T object) {
		if (object != null) {
			object = em.merge(object);
			em.remove(object);
		}
	}

	public void delete(List<T> objects) {
		for (T object : objects) {
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
		return EasyCriteriaFactory.createQueryCriteria(em, getType())
				.getResultList();
	}

	public List<T> findAll(int startIndex, int count) {
		EasyCriteria<T> criteria = EasyCriteriaFactory.createQueryCriteria(em,
				getType());
		criteria.setMaxResults(count);
		criteria.setFirstResult(startIndex);
		return criteria.getResultList();
	}

	public List<T> findAll(int startIndex, int count, String orderBy,
			boolean asc) {
		EasyCriteria<T> criteria = EasyCriteriaFactory.createQueryCriteria(em,
				getType());
		criteria.setMaxResults(count);
		criteria.setFirstResult(startIndex);
		if (asc) {
			criteria.orderByAsc(orderBy);
		} else {
			criteria.orderByDesc(orderBy);
		}
		return criteria.getResultList();
	}

	public long getCount() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<T> root = query.from(getType());
		query.select(builder.count(root));
		return em.createQuery(query).getSingleResult();
	}

	public <V> List<T> findByField(Attribute<T, V> field, V value) {
		EasyCriteria<T> criteria = createCriteria();
		criteria.andEquals(field.getName(), value);
		return criteria.getResultList();
	}

	protected void limit(TypedQuery<?> query, int offset, int limit) {
		if (offset > -1) {
			query.setFirstResult(offset);
		}
		if (limit > -1) {
			query.setMaxResults(limit);
		}
	}

	@SuppressWarnings("unchecked")
	protected Class<T> getType() {
		return (Class<T>) type.getRawType();
	}

	public EasyCriteria<T> createCriteria() {
		return EasyCriteriaFactory.createQueryCriteria(em, getType());
	}
}
