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
import com.google.api.client.util.Lists;
import com.uaihebert.model.EasyCriteria;

@Stateless
public class FeedEntryDAO extends GenericDAO<FeedEntry> {

	public List<FeedEntry> findByGuids(List<String> guids) {
		List<String> hashes = Lists.newArrayList();
		for (String guid : guids) {
			hashes.add(DigestUtils.sha1Hex(guid));
		}

		EasyCriteria<FeedEntry> criteria = createCriteria();
		criteria.setDistinctTrue();
		criteria.andStringIn(FeedEntry_.guidHash.getName(), hashes);
		criteria.leftJoinFetch(FeedEntry_.feeds.getName());

		List<FeedEntry> list = Lists.newArrayList();
		for (FeedEntry entry : criteria.getResultList()) {
			if (guids.contains(entry.getGuid())) {
				list.add(entry);
			}
		}
		return list;
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
