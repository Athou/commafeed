package com.commafeed.backend.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.commafeed.backend.dao.datamigrationtoggles.MigrationToggles;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.QFeed;
import com.commafeed.backend.model.QFeedSubscription;
import com.commafeed.backend.model.QUser;
import com.google.common.collect.Iterables;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.hibernate.HibernateQuery;

@Singleton
public class FeedDAO extends GenericDAO<Feed> {

	private QFeed feed = QFeed.feed;
	private HashMap<Long, Feed> longTermHashMap;

	@Inject
	public FeedDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public List<Feed> findNextUpdatable(int count, Date lastLoginThreshold) {
		HibernateQuery<Feed> query = query().selectFrom(feed);
		query.where(feed.disabledUntil.isNull().or(feed.disabledUntil.lt(new Date())));

		if (lastLoginThreshold != null) {
			QFeedSubscription subs = QFeedSubscription.feedSubscription;
			QUser user = QUser.user;

			JPQLQuery<Integer> subQuery = JPAExpressions.selectOne().from(subs);
			subQuery.join(subs.user, user).where(user.lastLogin.gt(lastLoginThreshold));
			query.where(subQuery.exists());
		}

		return query.orderBy(feed.disabledUntil.asc()).limit(count).distinct().fetch();
	}

	public Feed findByUrl(String normalizedUrl) {
		List<Feed> feeds = query().selectFrom(feed).where(feed.normalizedUrlHash.eq(DigestUtils.sha1Hex(normalizedUrl))).fetch();
		Feed feed = Iterables.getFirst(feeds, null);
		if (feed != null && StringUtils.equals(normalizedUrl, feed.getNormalizedUrl())) {
			return feed;
		}
		return null;
	}

	public List<Feed> findByTopic(String topic) {
		return query().selectFrom(feed).where(feed.pushTopicHash.eq(DigestUtils.sha1Hex(topic))).fetch();
	}

	public List<Feed> findWithoutSubscriptions(int max) {
		QFeedSubscription sub = QFeedSubscription.feedSubscription;
		return query().selectFrom(feed).where(JPAExpressions.selectOne().from(sub).where(sub.feed.eq(feed)).notExists()).limit(max)
				.fetch();
	}

	// Helper method findall()
	public List<Feed> findAll(){
		return query().selectFrom(feed).fetch();
	}

	public void forkLift(){
		if(MigrationToggles.isForkLiftOn()){
			List<Feed> feeds = findAll();
			for(Feed feed: feeds){
				saveOrUpdate(feed);
			}
		}
	}

	public int consistencyChecker() {
		int inconsistencyCounter = 0;
		if (MigrationToggles.isConsistencyCheckerOn()) {
			List<Feed> feeds = findAll();
			for(Feed f: feeds) {
				if (!this.storage.isModelConsistent(f)) {
					++inconsistencyCounter;
				}
			}
		}
		return inconsistencyCounter;
	}

	public void setLongTermHashMap(HashMap<Long, Feed> hashMap) {
		this.longTermHashMap = hashMap;
	}
}
