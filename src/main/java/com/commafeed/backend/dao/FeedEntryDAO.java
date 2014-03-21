package com.commafeed.backend.dao;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.apache.commons.codec.digest.DigestUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntry_;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.FeedSubscription_;
import com.commafeed.backend.model.Feed_;
import com.google.common.collect.Iterables;

@Stateless
public class FeedEntryDAO extends GenericDAO<FeedEntry> {

	public Long findExisting(String guid, Long feedId) {

		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<FeedEntry> root = query.from(getType());
		query.select(root.get(FeedEntry_.id));

		Predicate p1 = builder.equal(root.get(FeedEntry_.guidHash), DigestUtils.sha1Hex(guid));
		Predicate p2 = builder.equal(root.get(FeedEntry_.feed).get(Feed_.id), feedId);

		query.where(p1, p2);

		TypedQuery<Long> q = em.createQuery(query);
		limit(q, 0, 1);
		List<Long> list = q.getResultList();
		return Iterables.getFirst(list, null);
	}

	public List<FeedEntry> findWithoutSubscriptions(int max) {
		CriteriaQuery<FeedEntry> query = builder.createQuery(getType());
		Root<FeedEntry> root = query.from(getType());

		Join<FeedEntry, Feed> feedJoin = root.join(FeedEntry_.feed);
		SetJoin<Feed, FeedSubscription> subJoin = feedJoin.join(Feed_.subscriptions, JoinType.LEFT);
		query.where(builder.isNull(subJoin.get(FeedSubscription_.id)));
		TypedQuery<FeedEntry> q = em.createQuery(query);
		q.setMaxResults(max);

		return q.getResultList();
	}

	public int delete(Feed feed, int max) {

		CriteriaQuery<FeedEntry> query = builder.createQuery(getType());
		Root<FeedEntry> root = query.from(getType());

		query.where(builder.equal(root.get(FeedEntry_.feed), feed));
		TypedQuery<FeedEntry> q = em.createQuery(query);
		q.setMaxResults(max);

		List<FeedEntry> list = q.getResultList();
		int deleted = list.size();
		delete(list);
		return deleted;
	}

	public int delete(Date olderThan, int max) {
		CriteriaQuery<FeedEntry> query = builder.createQuery(getType());
		Root<FeedEntry> root = query.from(getType());
		query.where(builder.lessThan(root.get(FeedEntry_.inserted), olderThan));

		TypedQuery<FeedEntry> q = em.createQuery(query);
		q.setMaxResults(max);
		List<FeedEntry> list = q.getResultList();

		int deleted = list.size();
		delete(list);
		return deleted;
	}
}
