package com.commafeed.backend.service.internal;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.time.DateUtils;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedSubscriptionService;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class PostLoginActivities {

	private final UserDAO userDAO;
	private final FeedSubscriptionService feedSubscriptionService;
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
		if (config.getApplicationSettings().getHeavyLoad() && user.shouldRefreshFeedsAt(now)) {
			feedSubscriptionService.refreshAll(user);
			user.setLastFullRefresh(now);
			saveUser = true;
		}
		if (saveUser) {
			userDAO.saveOrUpdate(user);
		}
	}

}
