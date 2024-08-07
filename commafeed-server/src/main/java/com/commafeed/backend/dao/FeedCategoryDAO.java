package com.commafeed.backend.dao;

import java.util.List;
import java.util.Objects;

import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.QFeedCategory;
import com.commafeed.backend.model.QUser;
import com.commafeed.backend.model.User;
import com.querydsl.core.types.Predicate;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

@Singleton
public class FeedCategoryDAO extends GenericDAO<FeedCategory> {

	private static final QFeedCategory CATEGORY = QFeedCategory.feedCategory;

	public FeedCategoryDAO(EntityManager entityManager) {
		super(entityManager, FeedCategory.class);
	}

	public List<FeedCategory> findAll(User user) {
		return query().selectFrom(CATEGORY).where(CATEGORY.user.eq(user)).join(CATEGORY.user, QUser.user).fetchJoin().fetch();
	}

	public FeedCategory findById(User user, Long id) {
		return query().selectFrom(CATEGORY).where(CATEGORY.user.eq(user), CATEGORY.id.eq(id)).fetchOne();
	}

	public FeedCategory findByName(User user, String name, FeedCategory parent) {
		Predicate parentPredicate;
		if (parent == null) {
			parentPredicate = CATEGORY.parent.isNull();
		} else {
			parentPredicate = CATEGORY.parent.eq(parent);
		}
		return query().selectFrom(CATEGORY).where(CATEGORY.user.eq(user), CATEGORY.name.eq(name), parentPredicate).fetchOne();
	}

	public List<FeedCategory> findByParent(User user, FeedCategory parent) {
		Predicate parentPredicate;
		if (parent == null) {
			parentPredicate = CATEGORY.parent.isNull();
		} else {
			parentPredicate = CATEGORY.parent.eq(parent);
		}
		return query().selectFrom(CATEGORY).where(CATEGORY.user.eq(user), parentPredicate).fetch();
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
