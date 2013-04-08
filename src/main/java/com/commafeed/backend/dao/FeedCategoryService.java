package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import org.apache.commons.lang.ObjectUtils;

import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.uaihebert.model.EasyCriteria;

@Stateless
@SuppressWarnings("serial")
public class FeedCategoryService extends GenericDAO<FeedCategory> {

	public List<FeedCategory> findAll(User user) {
		EasyCriteria<FeedCategory> criteria = createCriteria();
		criteria.andEquals(MF.i(proxy().getUser()), user);

		criteria.innerJoinFetch(MF.i(proxy().getUser()));
		return criteria.getResultList();
	}

	public FeedCategory findById(User user, Long id) {
		EasyCriteria<FeedCategory> criteria = createCriteria();
		criteria.andEquals(MF.i(proxy().getUser()), user);
		criteria.andEquals(MF.i(proxy().getId()), id);
		return Iterables.getFirst(criteria.getResultList(), null);
	}

	public FeedCategory findByName(User user, String name, FeedCategory parent) {
		EasyCriteria<FeedCategory> criteria = createCriteria();
		criteria.andEquals(MF.i(proxy().getUser()), user);
		criteria.andEquals(MF.i(proxy().getName()), name);
		if (parent == null) {
			criteria.andIsNull(MF.i(proxy().getParent()));
		} else {
			criteria.andEquals(MF.i(proxy().getParent()), parent);
		}

		FeedCategory category = null;
		try {
			category = criteria.getSingleResult();
		} catch (NoResultException e) {
			category = null;
		}
		return category;
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
