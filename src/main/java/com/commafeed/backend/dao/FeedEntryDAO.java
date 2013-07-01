package com.commafeed.backend.dao;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.model.Feed;
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

	public static class EntryWithFeed {
		public FeedEntry entry;
		public Feed feed;

		public EntryWithFeed(FeedEntry entry, Feed feed) {
			this.entry = entry;
			this.feed = feed;
		}
	}

	public EntryWithFeed findExisting(String guid, String url, Long feedId) {

		TypedQuery<EntryWithFeed> q = em.createNamedQuery(
				"EntryStatus.existing", EntryWithFeed.class);
		q.setParameter("guidHash", DigestUtils.sha1Hex(guid));
		q.setParameter("url", url);
		q.setParameter("feedId", feedId);

		EntryWithFeed result = null;
		List<EntryWithFeed> list = q.getResultList();
		for (EntryWithFeed ewf : list) {
			if (ewf.entry != null && ewf.feed != null) {
				result = ewf;
				break;
			}
		}
		if (result == null) {
			result = Iterables.getFirst(list, null);
		}
		return result;
	}

	public List<FeedEntry> findByFeed(Feed feed, int offset, int limit) {
		CriteriaQuery<FeedEntry> query = builder.createQuery(getType());
		Root<FeedEntry> root = query.from(getType());
		SetJoin<FeedEntry, Feed> feedsJoin = root.join(FeedEntry_.feeds);

		query.where(builder.equal(feedsJoin.get(Feed_.id), feed.getId()));
		query.orderBy(builder.desc(root.get(FeedEntry_.updated)));
		TypedQuery<FeedEntry> q = em.createQuery(query);
		limit(q, offset, limit);
		setTimeout(q, applicationSettingsService.get().getQueryTimeout());
		return q.getResultList();
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
