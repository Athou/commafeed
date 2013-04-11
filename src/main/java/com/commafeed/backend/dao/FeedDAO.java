package com.commafeed.backend.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.time.DateUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.Feed_;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.common.collect.Iterables;
import com.uaihebert.model.EasyCriteria;

@Stateless
@SuppressWarnings("serial")
public class FeedDAO extends GenericDAO<Feed> {

	public List<Feed> findNextUpdatable(int count) {
		CriteriaQuery<Feed> query = builder.createQuery(getType());
		Root<Feed> root = query.from(getType());

		Date now = Calendar.getInstance().getTime();

		Predicate hasSubscriptions = builder.isNotEmpty(root
				.get(Feed_.subscriptions));

		Predicate neverUpdated = builder.isNull(root.get(Feed_.lastUpdated));
		Predicate updatedMoreThanOneMinuteAgo = builder.lessThan(
				root.get(Feed_.lastUpdated), DateUtils.addMinutes(now, -1));

		Predicate disabledDateIsNull = builder.isNull(root
				.get(Feed_.disabledUntil));
		Predicate DisabledDateIsInPast = builder.lessThan(
				root.get(Feed_.disabledUntil), now);

		query.where(hasSubscriptions,
				builder.or(neverUpdated, updatedMoreThanOneMinuteAgo),
				builder.or(disabledDateIsNull, DisabledDateIsInPast));
		query.orderBy(builder.asc(root.get(Feed_.lastUpdated)));

		TypedQuery<Feed> q = em.createQuery(query);
		q.setMaxResults(count);

		return q.getResultList();
	}

	public Feed findByUrl(String url) {
		List<Feed> feeds = findByField(MF.i(proxy().getUrl()), url);
		return Iterables.getFirst(feeds, null);
	}

	public Feed findByIdWithEntries(Long feedId, int offset, int limit) {
		EasyCriteria<Feed> criteria = createCriteria();
		criteria.andEquals(MF.i(proxy().getId()), feedId);
		criteria.leftJoinFetch(MF.i(proxy().getEntries()));

		criteria.setFirstResult(offset);
		criteria.setMaxResults(limit);
		return criteria.getSingleResult();
	}
}
