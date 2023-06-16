package com.commafeed.backend.service.internal;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.time.DateUtils;

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
	private final UnitOfWork unitOfWork;
	private final CommaFeedConfiguration config;

	public void executeFor(User user) {
		// only update lastLogin every once in a while in order to avoid invalidating the cache every time someone logs in
		Date now = new Date();
		Date lastLogin = user.getLastLogin();
		if (lastLogin == null || lastLogin.before(DateUtils.addMinutes(now, -30))) {
			user.setLastLogin(now);

			boolean heavyLoad = Boolean.TRUE.equals(config.getApplicationSettings().getHeavyLoad());
			if (heavyLoad) {
				// the amount of feeds in the database that are up for refresh might be very large since we're in heavy load mode
				// the feed refresh engine might not be able to catch up quickly enough
				// put feeds from online users that are up for refresh at the top of the queue
				feedSubscriptionService.refreshAllUpForRefresh(user);
			}

			// Post login activites are susceptible to run for any webservice call.
			// We update the user in a new transaction to update the user immediately.
			// If we didn't and the webservice call takes time, subsequent webservice calls would have to wait for the first call to
			// finish even if they didn't use the same database tables, because they updated the user too.
			unitOfWork.run(() -> userDAO.saveOrUpdate(user));
		}
	}
}
