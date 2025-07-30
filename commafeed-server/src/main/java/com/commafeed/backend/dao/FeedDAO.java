package com.commafeed.backend.dao;

import java.time.Instant;
import java.util.List;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

import org.apache.commons.lang3.Strings;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.QFeed;
import com.commafeed.backend.model.QFeedSubscription;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;

@Singleton
public class FeedDAO extends GenericDAO<Feed> {

	private static final QFeed FEED = QFeed.feed;
	private static final QFeedSubscription SUBSCRIPTION = QFeedSubscription.feedSubscription;

	public FeedDAO(EntityManager entityManager) {
		super(entityManager, Feed.class);
	}

	public List<Feed> findByIds(List<Long> id) {
		return query().selectFrom(FEED).where(FEED.id.in(id)).fetch();
	}

	public List<Feed> findNextUpdatable(int count, Instant lastLoginThreshold) {
		JPAQuery<Feed> query = query().selectFrom(FEED)
				.distinct()
				// join on subscriptions to only refresh feeds that have subscribers
				.join(SUBSCRIPTION)
				.on(SUBSCRIPTION.feed.eq(FEED))
				.where(FEED.disabledUntil.isNull().or(FEED.disabledUntil.lt(Instant.now())));

		if (lastLoginThreshold != null) {
			query.join(SUBSCRIPTION.user).where(SUBSCRIPTION.user.lastLogin.gt(lastLoginThreshold));
		}

		return query.orderBy(FEED.disabledUntil.asc()).limit(count).fetch();
	}

	public void setDisabledUntil(List<Long> feedIds, Instant date) {
		updateQuery(FEED).set(FEED.disabledUntil, date).where(FEED.id.in(feedIds)).execute();
	}

	public Feed findByUrl(String normalizedUrl, String normalizedUrlHash) {
		return query().selectFrom(FEED)
				.where(FEED.normalizedUrlHash.eq(normalizedUrlHash))
				.fetch()
				.stream()
				.filter(f -> Strings.CS.equals(normalizedUrl, f.getNormalizedUrl()))
				.findFirst()
				.orElse(null);
	}

	public List<Feed> findWithoutSubscriptions(int max) {
		QFeedSubscription sub = QFeedSubscription.feedSubscription;
		return query().selectFrom(FEED).where(JPAExpressions.selectOne().from(sub).where(sub.feed.eq(FEED)).notExists()).limit(max).fetch();
	}
}
