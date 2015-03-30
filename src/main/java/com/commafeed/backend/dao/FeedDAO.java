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
import com.mysema.query.BooleanBuilder;
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
		BooleanBuilder disabledDatePredicate = new BooleanBuilder();
		disabledDatePredicate.or(feed.disabledUntil.isNull());
		disabledDatePredicate.or(feed.disabledUntil.lt(new Date()));

		HibernateQuery query = null;
		if (lastLoginThreshold != null) {
			QFeedSubscription subs = QFeedSubscription.feedSubscription;
			QUser user = QUser.user;
			query = newQuery().from(subs);
			query.join(subs.feed, feed).join(subs.user, user).where(disabledDatePredicate, user.lastLogin.gt(lastLoginThreshold));
		} else {
			query = newQuery().from(feed);
			query.where(disabledDatePredicate);
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
