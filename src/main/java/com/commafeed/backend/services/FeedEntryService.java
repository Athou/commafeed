package com.commafeed.backend.services;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;

@Stateless
public class FeedEntryService {

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	@Inject
	FeedSubscriptionDAO feedSubscriptionDAO;

	@Inject
	FeedEntryDAO feedEntryDAO;

	public void markEntry(User user, Long entryId, Long subscriptionId,
			boolean read) {
		FeedSubscription sub = feedSubscriptionDAO.findById(user,
				subscriptionId);
		if (sub == null) {
			return;
		}

		FeedEntry entry = feedEntryDAO.findById(entryId);
		if (entry == null) {
			return;
		}

		FeedEntryStatus status = feedEntryStatusDAO.getStatus(sub, entry);
		if (status.isMarkable()) {
			status.setRead(read);
			feedEntryStatusDAO.saveOrUpdate(status);
		}
	}

	public void starEntry(User user, Long entryId, Long subscriptionId,
			boolean starred) {

		FeedSubscription sub = feedSubscriptionDAO.findById(user,
				subscriptionId);
		if (sub == null) {
			return;
		}

		FeedEntry entry = feedEntryDAO.findById(entryId);
		if (entry == null) {
			return;
		}

		FeedEntryStatus status = feedEntryStatusDAO.getStatus(sub, entry);
		if (status.isMarkable()) {
			status.setStarred(starred);
			feedEntryStatusDAO.saveOrUpdate(status);
		}

	}
}
