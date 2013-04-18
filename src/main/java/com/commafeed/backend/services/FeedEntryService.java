package com.commafeed.backend.services;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.User;

@Stateless
public class FeedEntryService {

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	public void markEntry(User user, Long entryId, boolean read) {
		FeedEntryStatus status = feedEntryStatusDAO.findById(user, entryId);
		status.setRead(read);
		feedEntryStatusDAO.update(status);
	}
}
