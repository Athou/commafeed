package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.FeedEntryTag_;
import com.commafeed.backend.model.FeedEntry_;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.User_;

@Stateless
public class FeedEntryTagDAO extends GenericDAO<FeedEntryTag> {

	public List<String> findByUser(User user) {
		CriteriaQuery<String> query = builder.createQuery(String.class);
		Root<FeedEntryTag> root = query.from(getType());
		query.select(root.get(FeedEntryTag_.name));
		query.distinct(true);

		Predicate p1 = builder.equal(root.get(FeedEntryTag_.user).get(User_.id), user.getId());
		query.where(p1);

		return cache(em.createQuery(query)).getResultList();
	}

	public List<FeedEntryTag> findByEntry(User user, FeedEntry entry) {
		CriteriaQuery<FeedEntryTag> query = builder.createQuery(getType());
		Root<FeedEntryTag> root = query.from(getType());

		Predicate p1 = builder.equal(root.get(FeedEntryTag_.user).get(User_.id), user.getId());
		Predicate p2 = builder.equal(root.get(FeedEntryTag_.entry).get(FeedEntry_.id), entry.getId());

		query.where(p1, p2);

		return cache(em.createQuery(query)).getResultList();
	}
}
