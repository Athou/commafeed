package com.commafeed.backend.dao;

import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.QFeed;
import com.commafeed.backend.model.QFeedSubscription;
import com.commafeed.backend.model.QUser;
import com.google.common.collect.Iterables;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.hibernate.HibernateQuery;
import com.mysema.query.jpa.hibernate.HibernateSubQuery;

public class FeedDAO extends GenericDAO<Feed> {

	private QFeed feed = QFeed.feed;

	public FeedDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public Long getUpdatableCount(Date lastLoginThreshold) {
		BooleanBuilder disabledDatePredicate = new BooleanBuilder();
		disabledDatePredicate.or(feed.disabledUntil.isNull());
		disabledDatePredicate.or(feed.disabledUntil.lt(new Date()));

		HibernateQuery query = newQuery().from(feed).where(disabledDatePredicate);
		if (lastLoginThreshold != null) {
			QFeedSubscription sub = QFeedSubscription.feedSubscription;
			QUser user = QUser.user;
			HibernateSubQuery subquery = new HibernateSubQuery();
			subquery.from(sub).join(sub.user, user).where(sub.feed.eq(feed), user.lastLogin.gt(lastLoginThreshold));
			query.where(subquery.exists());
		}
		return query.orderBy(feed.disabledUntil.asc()).count();
	}

	public List<Feed> findNextUpdatable(int count, Date lastLoginThreshold) {
		BooleanBuilder disabledDatePredicate = new BooleanBuilder();
		disabledDatePredicate.or(feed.disabledUntil.isNull());
		disabledDatePredicate.or(feed.disabledUntil.lt(new Date()));

		HibernateQuery query = newQuery().from(feed);
		if (lastLoginThreshold != null) {
			QFeedSubscription subs = QFeedSubscription.feedSubscription;
			QUser user = QUser.user;
			query.join(feed.subscriptions, subs).join(subs.user, user).where(disabledDatePredicate, user.lastLogin.gt(lastLoginThreshold));
		} else {
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
		return newQuery().from(feed).leftJoin(feed.subscriptions, sub).where(sub.id.isNull()).limit(max).list(feed);
	}
}
