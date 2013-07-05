package com.commafeed.backend.dao;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.feeds.FeedUtils;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.FeedSubscription_;
import com.commafeed.backend.model.Feed_;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Stateless
public class FeedDAO extends GenericDAO<Feed> {

	private List<Predicate> getUpdatablePredicates(Root<Feed> root,
			Date threshold) {

		Predicate hasSubscriptions = builder.isNotEmpty(root
				.get(Feed_.subscriptions));

		Predicate neverUpdated = builder.isNull(root.get(Feed_.lastUpdated));
		Predicate updatedBeforeThreshold = builder.lessThan(
				root.get(Feed_.lastUpdated), threshold);

		Predicate disabledDateIsNull = builder.isNull(root
				.get(Feed_.disabledUntil));
		Predicate disabledDateIsInPast = builder.lessThan(
				root.get(Feed_.disabledUntil), new Date());

		return Lists.newArrayList(hasSubscriptions,
				builder.or(neverUpdated, updatedBeforeThreshold),
				builder.or(disabledDateIsNull, disabledDateIsInPast));
	}

	public Long getUpdatableCount(Date threshold) {
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Feed> root = query.from(getType());

		query.select(builder.count(root));
		query.where(getUpdatablePredicates(root, threshold).toArray(
				new Predicate[0]));

		TypedQuery<Long> q = em.createQuery(query);
		return q.getSingleResult();
	}

	public List<Feed> findNextUpdatable(int count, Date threshold) {
		CriteriaQuery<Feed> query = builder.createQuery(getType());
		Root<Feed> root = query.from(getType());

		query.where(getUpdatablePredicates(root, threshold).toArray(
				new Predicate[0]));

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

		String normalized = FeedUtils.normalizeURL(url);
		feeds = findByField(Feed_.normalizedUrlHash,
				DigestUtils.sha1Hex(normalized));
		feed = Iterables.getFirst(feeds, null);
		if (feed != null
				&& StringUtils.equals(normalized, feed.getNormalizedUrl())) {
			return feed;
		}

		return null;
	}

	public List<Feed> findByTopic(String topic) {
		return findByField(Feed_.pushTopicHash, DigestUtils.sha1Hex(topic));
	}

	public int deleteWithoutSubscriptions(int max) {
		CriteriaQuery<Feed> query = builder.createQuery(getType());
		Root<Feed> root = query.from(getType());

		SetJoin<Feed, FeedSubscription> join = root.join(Feed_.subscriptions,
				JoinType.LEFT);
		query.where(builder.isNull(join.get(FeedSubscription_.id)));
		TypedQuery<Feed> q = em.createQuery(query);
		q.setMaxResults(max);

		List<Feed> list = q.getResultList();
		int deleted = list.size();

		for (Feed feed : list) {
			Query relationshipDeleteQuery = em
					.createNamedQuery("Feed.deleteEntryRelationships");
			relationshipDeleteQuery.setParameter("feedId", feed.getId());
			relationshipDeleteQuery.executeUpdate();
			delete(feed);
		}
		return deleted;

	}
}
