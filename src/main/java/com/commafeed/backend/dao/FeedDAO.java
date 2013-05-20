package com.commafeed.backend.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.Feed_;
import com.google.common.collect.Iterables;
import com.uaihebert.model.EasyCriteria;

@Stateless
public class FeedDAO extends GenericDAO<Feed> {

	public List<Feed> findNextUpdatable(int count) {
		CriteriaQuery<Feed> query = builder.createQuery(getType());
		Root<Feed> root = query.from(getType());

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

		query.where(hasSubscriptions,
				builder.or(neverUpdated, updatedBeforeThreshold),
				builder.or(disabledDateIsNull, disabledDateIsInPast));
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
		EasyCriteria<Feed> criteria = createCriteria();
		criteria.andEquals(Feed_.id.getName(), feedId);
		criteria.leftJoinFetch(Feed_.entries.getName());

		criteria.setFirstResult(offset);
		criteria.setMaxResults(limit);
		return criteria.getSingleResult();
	}
}
