package com.commafeed.backend.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.Feed_;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Stateless
public class FeedDAO extends GenericDAO<Feed> {

	private List<Predicate> getUpdatablePredicates(Root<Feed> root) {
		Date now = Calendar.getInstance().getTime();

		Predicate hasSubscriptions = builder.isNotEmpty(root
				.get(Feed_.subscriptions));

		Predicate neverUpdated = builder.isNull(root.get(Feed_.lastUpdated));
		Predicate updatedBeforeThreshold = builder.lessThan(
				root.get(Feed_.lastUpdated), DateUtils.addMinutes(now, -10));

		Predicate disabledDateIsNull = builder.isNull(root
				.get(Feed_.disabledUntil));
		Predicate disabledDateIsInPast = builder.lessThan(
				root.get(Feed_.disabledUntil), now);

		return Lists.newArrayList(hasSubscriptions,
				builder.or(neverUpdated, updatedBeforeThreshold),
				builder.or(disabledDateIsNull, disabledDateIsInPast));
	}

	public Long getUpdatableCount() {
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Feed> root = query.from(getType());

		query.select(builder.count(root));
		query.where(getUpdatablePredicates(root).toArray(new Predicate[0]));

		TypedQuery<Long> q = em.createQuery(query);
		return q.getSingleResult();
	}

	public List<Feed> findNextUpdatable(int count) {
		CriteriaQuery<Feed> query = builder.createQuery(getType());
		Root<Feed> root = query.from(getType());

		query.where(getUpdatablePredicates(root).toArray(new Predicate[0]));

		query.orderBy(builder.asc(root.get(Feed_.lastUpdated)));

		TypedQuery<Feed> q = em.createQuery(query);
		q.setMaxResults(count);

		return q.getResultList();
	}

	public Feed findByUrl(String url) {
		List<Feed> feeds = findByField(Feed_.urlHash, DigestUtils.sha1Hex(url));
		Feed feed = Iterables.getFirst(feeds, null);
		if (feed != null && StringUtils.equals(url, feed.getUrl())) {
			return feed;
		}
		return null;
	}

	public Feed findByIdWithEntries(Long feedId, int offset, int limit) {
		CriteriaQuery<Feed> query = builder.createQuery(getType());
		Root<Feed> root = query.from(getType());

		query.where(builder.equal(root.get(Feed_.id), feedId));
		root.fetch(Feed_.entries, JoinType.LEFT);

		TypedQuery<Feed> q = em.createQuery(query);
		limit(q, offset, limit);
		return q.getSingleResult();
	}

	public List<Feed> findByTopic(String topic) {
		return findByField(Feed_.pushTopicHash, DigestUtils.sha1Hex(topic));
	}
}
