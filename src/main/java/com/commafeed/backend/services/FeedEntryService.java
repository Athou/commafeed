package com.commafeed.backend.services;

import javax.ejb.Stateless;
import javax.inject.Inject;

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

	public void markEntry(User user, Long entryId, Long subscriptionId,
			boolean read) {
		FeedSubscription sub = feedSubscriptionDAO.findById(user,
				subscriptionId);
		if (sub == null) {
			return;
		}

		FeedEntry entry = new FeedEntry();
		entry.setId(entryId);

		FeedEntryStatus status = feedEntryStatusDAO.getStatus(sub, entry);

		if (read) {
			if (status.getId() != null) {
				if (status.isStarred()) {
					status.setRead(true);
					feedEntryStatusDAO.saveOrUpdate(status);
				} else {
					feedEntryStatusDAO.delete(status);
				}
			}
		} else {
			if (status.getId() == null) {
				status = new FeedEntryStatus(user, sub, entry);
				status.setSubscription(sub);
			}
			status.setRead(false);
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

		FeedEntry entry = new FeedEntry();
		entry.setId(entryId);

		FeedEntryStatus status = feedEntryStatusDAO.getStatus(sub, entry);

		if (!starred) {
			if (status.getId() != null) {
				if (!status.isRead()) {
					status.setStarred(false);
					feedEntryStatusDAO.saveOrUpdate(status);
				} else {
					feedEntryStatusDAO.delete(status);
				}
			}
		} else {
			if (status.getId() == null) {
				status = new FeedEntryStatus(user, sub, entry);
				status.setSubscription(sub);
				status.setRead(true);
			}
			status.setStarred(true);
			feedEntryStatusDAO.saveOrUpdate(status);
		}
	}
}
