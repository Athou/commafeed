package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntry_;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.uaihebert.model.EasyCriteria;

@Stateless
@SuppressWarnings("serial")
public class FeedEntryDAO extends GenericDAO<FeedEntry> {

	public List<FeedEntry> findByGuids(List<String> guids) {
		EasyCriteria<FeedEntry> criteria = createCriteria();
		criteria.setDistinctTrue();
		criteria.andStringIn(MF.i(proxy().getGuid()), guids);
		criteria.leftJoinFetch(MF.i(proxy().getFeeds()));
		return criteria.getResultList();
	}

	public List<FeedEntry> findByFeed(Feed feed, int offset, int limit) {
		CriteriaQuery<FeedEntry> query = builder.createQuery(getType());
		Root<FeedEntry> root = query.from(getType());
		query.where(builder.isMember(feed, root.get(FeedEntry_.feeds)));
		query.orderBy(builder.desc(root.get(FeedEntry_.updated)));
		TypedQuery<FeedEntry> q = em.createQuery(query);
		q.setFirstResult(offset);
		q.setMaxResults(limit);
		return q.getResultList();
	}
}
