package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryContent_;
import com.commafeed.backend.model.FeedEntry_;
import com.google.common.collect.Iterables;

@Stateless
public class FeedEntryContentDAO extends GenericDAO<FeedEntryContent> {

	public Long findExisting(String contentHash, String titleHash) {

		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<FeedEntryContent> root = query.from(getType());
		query.select(root.get(FeedEntryContent_.id));

		Predicate p1 = builder.equal(root.get(FeedEntryContent_.contentHash), contentHash);
		Predicate p2 = builder.equal(root.get(FeedEntryContent_.titleHash), titleHash);

		query.where(p1, p2);
		TypedQuery<Long> q = em.createQuery(query);
		limit(q, 0, 1);
		return Iterables.getFirst(q.getResultList(), null);

	}

	public int deleteWithoutEntries(int max) {
		CriteriaQuery<FeedEntryContent> query = builder.createQuery(getType());
		Root<FeedEntryContent> root = query.from(getType());

		Join<FeedEntryContent, FeedEntry> join = root.join(FeedEntryContent_.entries, JoinType.LEFT);
		query.where(builder.isNull(join.get(FeedEntry_.id)));
		TypedQuery<FeedEntryContent> q = em.createQuery(query);
		q.setMaxResults(max);

		List<FeedEntryContent> list = q.getResultList();
		int deleted = list.size();
		delete(list);
		return deleted;

	}
}
