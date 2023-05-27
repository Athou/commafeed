package com.commafeed.backend.service.internal;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.SessionFactory;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedSubscriptionService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class PostLoginActivities {

	private final UserDAO userDAO;
	private final FeedSubscriptionService feedSubscriptionService;
	private final SessionFactory sessionFactory;
	private final CommaFeedConfiguration config;

	public void executeFor(User user) {
		Date lastLogin = user.getLastLogin();
		Date now = new Date();

		boolean saveUser = false;

		// only update lastLogin field every hour in order to not
		// invalidate the cache every time someone logs in
		if (lastLogin == null || lastLogin.before(DateUtils.addHours(now, -1))) {
			user.setLastLogin(now);
			saveUser = true;
		}

		if (Boolean.TRUE.equals(config.getApplicationSettings().getHeavyLoad()) && user.shouldRefreshFeedsAt(now)) {
			feedSubscriptionService.refreshAll(user);
			user.setLastFullRefresh(now);
			saveUser = true;
		}

		if (saveUser) {
			// Post login activites are susceptible to run for any webservice call.
			// We update the user in a new transaction to update the user immediately.
			// If we didn't and the webservice call takes time, subsequent webservice calls would have to wait for the first call to
			// finish even if they didn't use the same database tables, because they updated the user too.
			UnitOfWork.run(sessionFactory, () -> userDAO.saveOrUpdate(user));
		}
	}

}
