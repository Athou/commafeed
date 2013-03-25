package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import org.apache.commons.lang.ObjectUtils;

import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.common.collect.Iterables;
import com.uaihebert.model.EasyCriteria;

@Stateless
@SuppressWarnings("serial")
public class FeedCategoryService extends GenericDAO<FeedCategory, Long> {

	public List<FeedCategory> findAll(User user) {
		return findByField(MF.i(MF.p(FeedCategory.class).getUser()), user);
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
