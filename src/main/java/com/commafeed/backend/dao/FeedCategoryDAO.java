package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.ObjectUtils;

import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedCategory_;
import com.commafeed.backend.model.User;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Stateless
public class FeedCategoryDAO extends GenericDAO<FeedCategory> {

	public List<FeedCategory> findAll(User user) {

		CriteriaQuery<FeedCategory> query = builder.createQuery(getType());
		Root<FeedCategory> root = query.from(getType());

		query.where(builder.equal(root.get(FeedCategory_.user), user));
		root.fetch(FeedCategory_.user.getName());

		return em.createQuery(query).getResultList();
	}

	public FeedCategory findById(User user, Long id) {
		CriteriaQuery<FeedCategory> query = builder.createQuery(getType());
		Root<FeedCategory> root = query.from(getType());

		Predicate p1 = builder.equal(root.get(FeedCategory_.user), user);
		Predicate p2 = builder.equal(root.get(FeedCategory_.id), id);

		query.where(p1, p2);

		return Iterables.getFirst(em.createQuery(query).getResultList(), null);
	}

	public FeedCategory findByName(User user, String name, FeedCategory parent) {
		CriteriaQuery<FeedCategory> query = builder.createQuery(getType());
		Root<FeedCategory> root = query.from(getType());

		List<Predicate> predicates = Lists.newArrayList();

		predicates.add(builder.equal(root.get(FeedCategory_.user), user));
		predicates.add(builder.equal(root.get(FeedCategory_.name), name));

		if (parent == null) {
			predicates.add(builder.isNull(root.get(FeedCategory_.parent)));
		} else {
			predicates
					.add(builder.equal(root.get(FeedCategory_.parent), parent));
		}

		query.where(predicates.toArray(new Predicate[0]));

		FeedCategory category = null;
		try {
			category = em.createQuery(query).getSingleResult();
		} catch (NoResultException e) {
			category = null;
		}
		return category;
	}

	public List<FeedCategory> findByParent(User user, FeedCategory parent) {
		CriteriaQuery<FeedCategory> query = builder.createQuery(getType());
		Root<FeedCategory> root = query.from(getType());

		List<Predicate> predicates = Lists.newArrayList();

		predicates.add(builder.equal(root.get(FeedCategory_.user), user));
		if (parent == null) {
			predicates.add(builder.isNull(root.get(FeedCategory_.parent)));
		} else {
			predicates
					.add(builder.equal(root.get(FeedCategory_.parent), parent));
		}

		query.where(predicates.toArray(new Predicate[0]));

		return em.createQuery(query).getResultList();
	}

	public List<FeedCategory> findAllChildrenCategories(User user,
			FeedCategory parent) {
		List<FeedCategory> list = Lists.newArrayList();
		List<FeedCategory> all = findAll(user);
		for (FeedCategory cat : all) {
			if (isChild(cat, parent)) {
				list.add(cat);
			}
		}
		return list;
	}

	public boolean isChild(FeedCategory child, FeedCategory parent) {
		if (parent == null) {
			return true;
		}
		boolean isChild = false;
		while (child != null) {
			if (ObjectUtils.equals(child.getId(), parent.getId())) {
				isChild = true;
				break;
			}
			child = child.getParent();
		}
		return isChild;
	}

}
