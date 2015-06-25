package com.commafeed.backend.dao;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.QFeed;
import com.commafeed.backend.model.QFeedSubscription;
import com.commafeed.backend.model.QUser;
import com.google.common.collect.Iterables;
import com.mysema.query.jpa.hibernate.HibernateQuery;
import com.mysema.query.jpa.hibernate.HibernateSubQuery;

@Singleton
public class FeedDAO extends GenericDAO<Feed> {

	private QFeed feed = QFeed.feed;

	@Inject
	public FeedDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public List<Feed> findNextUpdatable(int count, Date lastLoginThreshold) {
		HibernateQuery query = newQuery().from(feed);
		query.where(feed.disabledUntil.isNull().or(feed.disabledUntil.lt(new Date())));

		if (lastLoginThreshold != null) {
			QFeedSubscription subs = QFeedSubscription.feedSubscription;
			QUser user = QUser.user;

			HibernateSubQuery subQuery = new HibernateSubQuery().from(subs);
			subQuery.join(subs.user, user).where(user.lastLogin.gt(lastLoginThreshold));
			query.where(subQuery.exists());
		}

		return query.orderBy(feed.disabledUntil.asc()).limit(count).distinct().list(feed);
	}

	public Feed findByUrl(String normalizedUrl) {
		List<Feed> feeds = newQuery().from(feed).where(feed.normalizedUrlHash.eq(DigestUtils.sha1Hex(normalizedUrl))).list(feed);
		Feed feed = Iterables.getFirst(feeds, null);
		if (feed != null && StringUtils.equals(normalizedUrl, feed.getNormalizedUrl())) {
			return feed;
		}
		return null;
	}

	public List<Feed> findByTopic(String topic) {
		return newQuery().from(feed).where(feed.pushTopicHash.eq(DigestUtils.sha1Hex(topic))).list(feed);
	}

	public List<Feed> findWithoutSubscriptions(int max) {
		QFeedSubscription sub = QFeedSubscription.feedSubscription;
		return newQuery().from(feed).where(new HibernateSubQuery().from(sub).where(sub.feed.eq(feed)).notExists()).limit(max).list(feed);
		// return newQuery().from(feed).leftJoin(feed.subscriptions, sub).where(sub.id.isNull()).limit(max).list(feed);
	}
}
