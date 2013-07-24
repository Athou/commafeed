package com.commafeed.backend.dao;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntry_;
import com.commafeed.backend.model.Feed_;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.google.common.collect.Iterables;

@Stateless
public class FeedEntryDAO extends GenericDAO<FeedEntry> {

	@Inject
	ApplicationSettingsService applicationSettingsService;

	protected static final Logger log = LoggerFactory
			.getLogger(FeedEntryDAO.class);

	public FeedEntry findExisting(String guid, String url, Long feedId) {

		CriteriaQuery<FeedEntry> query = builder.createQuery(getType());
		Root<FeedEntry> root = query.from(getType());

		Predicate p1 = builder.equal(root.get(FeedEntry_.guidHash),
				DigestUtils.sha1Hex(guid));
		Predicate p2 = builder.equal(root.get(FeedEntry_.url), url);
		Predicate p3 = builder.equal(root.get(FeedEntry_.feed).get(Feed_.id),
				feedId);

		query.where(p1, p2, p3);

		List<FeedEntry> list = em.createQuery(query).getResultList();
		return Iterables.getFirst(list, null);
	}

	public int delete(Date olderThan, int max) {
		CriteriaQuery<FeedEntry> query = builder.createQuery(getType());
		Root<FeedEntry> root = query.from(getType());
		query.where(builder.lessThan(root.get(FeedEntry_.inserted), olderThan));

		TypedQuery<FeedEntry> q = em.createQuery(query);
		q.setMaxResults(max);
		List<FeedEntry> list = q.getResultList();

		int deleted = list.size();
		delete(list);
		return deleted;
	}
}
