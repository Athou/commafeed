package com.commafeed.backend.dao;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.codec.digest.DigestUtils;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryContent_;
import com.commafeed.backend.model.FeedEntry_;
import com.google.common.collect.Iterables;

public class FeedEntryContentDAO extends GenericDAO<FeedEntryContent> {

	public FeedEntryContent findExisting(FeedEntryContent content) {

		CriteriaQuery<FeedEntryContent> query = builder.createQuery(getType());
		Root<FeedEntryContent> root = query.from(getType());

		Predicate p1 = builder.equal(root.get(FeedEntryContent_.contentHash),
				DigestUtils.sha1Hex(content.getContent()));
		Predicate p2 = null;
		if (content.getTitle() == null) {
			p2 = builder.isNull(root.get(FeedEntryContent_.title));
		} else {
			p2 = builder.equal(root.get(FeedEntryContent_.title),
					content.getTitle());
		}

		Predicate p3 = null;
		if (content.getAuthor() == null) {
			p3 = builder.isNull(root.get(FeedEntryContent_.author));
		} else {
			p3 = builder.equal(root.get(FeedEntryContent_.author),
					content.getAuthor());
		}

		query.where(p1, p2, p3);
		TypedQuery<FeedEntryContent> q = em.createQuery(query);
		return Iterables.getFirst(q.getResultList(), null);

	}

	public int deleteWithoutEntries(int max) {
		CriteriaQuery<FeedEntryContent> query = builder.createQuery(getType());
		Root<FeedEntryContent> root = query.from(getType());

		Join<FeedEntryContent, FeedEntry> join = root.join(
				FeedEntryContent_.entries, JoinType.LEFT);
		query.where(builder.isNull(join.get(FeedEntry_.id)));
		TypedQuery<FeedEntryContent> q = em.createQuery(query);
		q.setMaxResults(max);

		List<FeedEntryContent> list = q.getResultList();
		int deleted = list.size();
		return deleted;

	}
}
