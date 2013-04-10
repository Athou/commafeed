package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;

import com.commafeed.backend.model.Feed;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.common.collect.Iterables;
import com.uaihebert.model.EasyCriteria;

@Stateless
@SuppressWarnings("serial")
public class FeedService extends GenericDAO<Feed> {

	public List<Feed> findNextUpdatable(int count) {
		EasyCriteria<Feed> criteria = createCriteria();
		criteria.orderByAsc(MF.i(proxy().getLastUpdated()));
		criteria.setMaxResults(count);
		return criteria.getResultList();
	}

	public Feed findByUrl(String url) {
		List<Feed> feeds = findByField(MF.i(proxy().getUrl()), url);
		return Iterables.getFirst(feeds, null);
	}

	public Feed getByIdWithEntries(Long feedId, int offset, int limit) {
		EasyCriteria<Feed> criteria = createCriteria();
		criteria.andEquals(MF.i(proxy().getId()), feedId);
		criteria.leftJoinFetch(MF.i(proxy().getEntries()));
		
		criteria.setFirstResult(offset);
		criteria.setMaxResults(limit);
		return criteria.getSingleResult();
	}
}
