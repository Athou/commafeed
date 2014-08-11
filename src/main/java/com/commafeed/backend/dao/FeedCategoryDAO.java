package com.commafeed.backend.dao;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.SessionFactory;

import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.QFeedCategory;
import com.commafeed.backend.model.QUser;
import com.commafeed.backend.model.User;
import com.google.common.collect.Lists;
import com.mysema.query.types.Predicate;

public class FeedCategoryDAO extends GenericDAO<FeedCategory> {

	private QFeedCategory category = QFeedCategory.feedCategory;

	public FeedCategoryDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public List<FeedCategory> findAll(User user) {
		return newQuery().from(category).where(category.user.eq(user)).join(category.user, QUser.user).fetch().list(category);
	}

	public FeedCategory findById(User user, Long id) {
		return newQuery().from(category).where(category.user.eq(user), category.id.eq(id)).uniqueResult(category);
	}

	public FeedCategory findByName(User user, String name, FeedCategory parent) {
		Predicate parentPredicate = null;
		if (parent == null) {
			parentPredicate = category.parent.isNull();
		} else {
			parentPredicate = category.parent.eq(parent);
		}
		return newQuery().from(category).where(category.user.eq(user), category.name.eq(name), parentPredicate).uniqueResult(category);
	}

	public List<FeedCategory> findByParent(User user, FeedCategory parent) {
		Predicate parentPredicate = null;
		if (parent == null) {
			parentPredicate = category.parent.isNull();
		} else {
			parentPredicate = category.parent.eq(parent);
		}
		return newQuery().from(category).where(category.user.eq(user), parentPredicate).list(category);
	}

	public List<FeedCategory> findAllChildrenCategories(User user, FeedCategory parent) {
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
