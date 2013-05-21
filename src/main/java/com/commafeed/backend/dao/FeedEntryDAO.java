package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.apache.commons.codec.digest.DigestUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntry_;
import com.commafeed.backend.model.Feed_;
import com.uaihebert.model.EasyCriteria;

@Stateless
public class FeedEntryDAO extends GenericDAO<FeedEntry> {

	public List<FeedEntry> findByGuid(String guid) {
		String hash = DigestUtils.sha1Hex(guid);

		EasyCriteria<FeedEntry> criteria = createCriteria();
		criteria.setDistinctTrue();
		criteria.andEquals(FeedEntry_.guidHash.getName(), hash);
		criteria.leftJoinFetch(FeedEntry_.feeds.getName());

		return criteria.getResultList();
	}

	public List<FeedEntry> findByFeed(Feed feed, int offset, int limit) {
		CriteriaQuery<FeedEntry> query = builder.createQuery(getType());
		Root<FeedEntry> root = query.from(getType());
		SetJoin<FeedEntry, Feed> feedsJoin = root.join(FeedEntry_.feeds);
		query.where(builder.equal(feedsJoin.get(Feed_.id), feed.getId()));
		query.orderBy(builder.desc(root.get(FeedEntry_.updated)));
		TypedQuery<FeedEntry> q = em.createQuery(query);
		limit(q, offset, limit);
		return q.getResultList();
	}
}
