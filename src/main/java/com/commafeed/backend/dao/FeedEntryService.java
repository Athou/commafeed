package com.commafeed.backend.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedEntry_;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.common.collect.Lists;
import com.uaihebert.model.EasyCriteria;

@Stateless
@SuppressWarnings("serial")
public class FeedEntryService extends GenericDAO<FeedEntry> {

	@Inject
	FeedService feedService;

	@Inject
	FeedSubscriptionService feedSubscriptionService;

	public void updateEntries(String url, Collection<FeedEntry> entries) {
		Feed feed = feedService.findByUrl(url);
		List<String> guids = Lists.newArrayList();
		for (FeedEntry entry : entries) {
			guids.add(entry.getGuid());
		}

		List<FeedEntry> existingEntries = guids.isEmpty() ? new ArrayList<FeedEntry>()
				: findByGuids(guids);
		for (FeedEntry entry : entries) {
			FeedEntry foundEntry = null;
			for (FeedEntry existingEntry : existingEntries) {
				if (StringUtils
						.equals(entry.getGuid(), existingEntry.getGuid())) {
					foundEntry = existingEntry;
					break;
				}
			}
			if (foundEntry == null) {
				entry.setInserted(Calendar.getInstance().getTime());
				addFeedToEntry(entry, feed);
			} else {
				boolean foundFeed = false;
				for (Feed existingFeed : foundEntry.getFeeds()) {
					if (ObjectUtils.equals(existingFeed.getId(), feed.getId())) {
						foundFeed = true;
						break;
					}
				}

				if (!foundFeed) {
					addFeedToEntry(foundEntry, feed);
				}
			}
		}
	}

	private void addFeedToEntry(FeedEntry entry, Feed feed) {
		entry.getFeeds().add(feed);
		saveOrUpdate(entry);
		List<FeedSubscription> subscriptions = feedSubscriptionService
				.findByFeed(feed);
		for (FeedSubscription sub : subscriptions) {
			FeedEntryStatus status = new FeedEntryStatus();
			status.setEntry(entry);
			status.setSubscription(sub);
			em.persist(status);
		}

	}

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
