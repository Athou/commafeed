package com.commafeed.backend.dao;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.feeds.FeedUtils;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.FeedSubscription_;
import com.commafeed.backend.model.Feed_;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.User_;
import com.commafeed.frontend.model.FeedCount;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Stateless
public class FeedDAO extends GenericDAO<Feed> {

	private List<Predicate> getUpdatablePredicates(CriteriaQuery<?> query, Root<Feed> root, Date lastLoginThreshold) {

		List<Predicate> preds = Lists.newArrayList();
		Predicate isNull = builder.isNull(root.get(Feed_.disabledUntil));
		Predicate lessThan = builder.lessThan(root.get(Feed_.disabledUntil), new Date());
		preds.add(builder.or(isNull, lessThan));

		if (lastLoginThreshold != null) {
			Subquery<Long> subquery = query.subquery(Long.class);
			Root<FeedSubscription> subroot = subquery.from(FeedSubscription.class);
			subquery.select(builder.count(subroot.get(FeedSubscription_.id)));

			Join<FeedSubscription, User> userJoin = subroot.join(FeedSubscription_.user);
			Predicate p1 = builder.equal(subroot.get(FeedSubscription_.feed), root);
			Predicate p2 = builder.greaterThanOrEqualTo(userJoin.get(User_.lastLogin), lastLoginThreshold);
			subquery.where(p1, p2);

			preds.add(builder.exists(subquery));
		}

		return preds;
	}

	public Long getUpdatableCount(Date lastLoginThreshold) {
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Feed> root = query.from(getType());

		query.select(builder.count(root));
		query.where(getUpdatablePredicates(query, root, lastLoginThreshold).toArray(new Predicate[0]));

		TypedQuery<Long> q = em.createQuery(query);
		return q.getSingleResult();
	}

	public List<Feed> findNextUpdatable(int count, Date lastLoginThreshold) {
		CriteriaQuery<Feed> query = builder.createQuery(getType());
		Root<Feed> root = query.from(getType());

		query.where(getUpdatablePredicates(query, root, lastLoginThreshold).toArray(new Predicate[0]));
		query.orderBy(builder.asc(root.get(Feed_.disabledUntil)));

		TypedQuery<Feed> q = em.createQuery(query);
		q.setMaxResults(count);

		return q.getResultList();
	}

	public Feed findByUrl(String url) {

		String normalized = FeedUtils.normalizeURL(url);
		List<Feed> feeds = findByField(Feed_.normalizedUrlHash, DigestUtils.sha1Hex(normalized));
		Feed feed = Iterables.getFirst(feeds, null);
		if (feed != null && StringUtils.equals(normalized, feed.getNormalizedUrl())) {
			return feed;
		}

		return null;
	}

	public List<Feed> findByTopic(String topic) {
		return findByField(Feed_.pushTopicHash, DigestUtils.sha1Hex(topic));
	}
	
	public List<Feed> findWithoutSubscriptions(int max) {
		CriteriaQuery<Feed> query = builder.createQuery(getType());
		Root<Feed> root = query.from(getType());

		SetJoin<Feed, FeedSubscription> join = root.join(Feed_.subscriptions, JoinType.LEFT);
		query.where(builder.isNull(join.get(FeedSubscription_.id)));
		TypedQuery<Feed> q = em.createQuery(query);
		q.setMaxResults(max);

		return q.getResultList();
	}

	public static enum DuplicateMode {
		NORMALIZED_URL(Feed_.normalizedUrlHash), LAST_CONTENT(Feed_.lastContentHash), PUSH_TOPIC(Feed_.pushTopicHash);
		private SingularAttribute<Feed, String> path;

		private DuplicateMode(SingularAttribute<Feed, String> path) {
			this.path = path;
		}

		public SingularAttribute<Feed, String> getPath() {
			return path;
		}
	}

	public List<FeedCount> findDuplicates(DuplicateMode mode, int offset, int limit, long minCount) {
		CriteriaQuery<String> query = builder.createQuery(String.class);
		Root<Feed> root = query.from(getType());

		Path<String> path = root.get(mode.getPath());
		Expression<Long> count = builder.count(path);

		query.select(path);

		query.groupBy(path);
		query.having(builder.greaterThan(count, minCount));

		TypedQuery<String> q = em.createQuery(query);
		limit(q, offset, limit);
		List<String> pathValues = q.getResultList();

		List<FeedCount> result = Lists.newArrayList();
		for (String pathValue : pathValues) {
			FeedCount fc = new FeedCount(pathValue);
			for (Feed feed : findByField(mode.getPath(), pathValue)) {
				Feed f = new Feed();
				f.setId(feed.getId());
				f.setUrl(feed.getUrl());
				fc.getFeeds().add(f);
			}
			result.add(fc);
		}
		return result;
	}
}
