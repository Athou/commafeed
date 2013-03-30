package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.uaihebert.factory.EasyCriteriaFactory;
import com.uaihebert.model.EasyCriteria;

@Stateless
@SuppressWarnings("serial")
public class FeedEntryStatusService extends GenericDAO<FeedEntryStatus> {

	public List<FeedEntryStatus> findAll(User user) {
		return findByField(MF.i(proxy().getUser()), user);
	}

	public FeedEntryStatus getStatus(User user, FeedEntry entry) {
		EasyCriteria<FeedEntryStatus> criteria = EasyCriteriaFactory
				.createQueryCriteria(em, getType());
		criteria.andEquals(MF.i(proxy().getUser()), user);
		criteria.andEquals(MF.i(proxy().getEntry()), entry);

		FeedEntryStatus status = null;
		try {
			status = criteria.getSingleResult();
		} catch (NoResultException e) {
			status = null;
		}
		return status;
	}
}
