package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.commafeed.backend.model.FeedPushInfo;
import com.commafeed.backend.model.FeedPushInfo_;

@Stateless
public class FeedPushInfoDAO extends GenericDAO<FeedPushInfo> {

	public List<FeedPushInfo> findByTopic(String topic) {

		CriteriaQuery<FeedPushInfo> query = builder.createQuery(getType());
		Root<FeedPushInfo> root = query.from(getType());
		root.fetch(FeedPushInfo_.feed);
		query.where(builder.equal(root.get(FeedPushInfo_.topic), topic));

		TypedQuery<FeedPushInfo> q = em.createQuery(query);
		return q.getResultList();
	}

}
