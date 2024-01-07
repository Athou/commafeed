package com.commafeed.backend.dao;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.QFeed;
import com.commafeed.backend.model.QFeedSubscription;
import com.google.common.collect.Iterables;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class FeedDAO extends GenericDAO<Feed> {

	private final QFeed feed = QFeed.feed;
	private final QFeedSubscription subscription = QFeedSubscription.feedSubscription;

	@Inject
	public FeedDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public List<Feed> findNextUpdatable(int count, Date lastLoginThreshold) {
		JPAQuery<Feed> query = query().selectFrom(feed).where(feed.disabledUntil.isNull().or(feed.disabledUntil.lt(new Date())));
		if (lastLoginThreshold != null) {
			query.where(JPAExpressions.selectOne()
					.from(subscription)
					.join(subscription.user)
					.where(subscription.feed.id.eq(feed.id), subscription.user.lastLogin.gt(lastLoginThreshold))
					.exists());
		}

		return query.orderBy(feed.disabledUntil.asc()).limit(count).fetch();
	}

	public void setDisabledUntil(List<Long> feedIds, Date date) {
		updateQuery(feed).set(feed.disabledUntil, date).where(feed.id.in(feedIds)).execute();
	}

	public Feed findByUrl(String normalizedUrl, String normalizedUrlHash) {
		List<Feed> feeds = query().selectFrom(feed).where(feed.normalizedUrlHash.eq(normalizedUrlHash)).fetch();
		Feed feed = Iterables.getFirst(feeds, null);
		if (feed != null && StringUtils.equals(normalizedUrl, feed.getNormalizedUrl())) {
			return feed;
		}
		return null;
	}

	public List<Feed> findWithoutSubscriptions(int max) {
		QFeedSubscription sub = QFeedSubscription.feedSubscription;
		return query().selectFrom(feed).where(JPAExpressions.selectOne().from(sub).where(sub.feed.eq(feed)).notExists()).limit(max).fetch();
	}
}
