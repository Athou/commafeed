package com.commafeed.backend.task;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.commafeed.CommaFeedApplication;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
@Slf4j
public class DemoAccountCleanupTask extends ScheduledTask {

	private final CommaFeedConfiguration config;
	private final UnitOfWork unitOfWork;
	private final UserDAO userDAO;
	private final UserService userService;

	@Override
	protected void run() {
		if (!config.getApplicationSettings().getCreateDemoAccount()) {
			return;
		}

		log.info("recreating demo user account");
		unitOfWork.run(() -> {
			User demoUser = userDAO.findByName(CommaFeedApplication.USERNAME_DEMO);
			if (demoUser == null) {
				return;
			}

			userService.unregister(demoUser);
			userService.createDemoUser();
		});

	}

	@Override
	protected long getInitialDelay() {
		return 1;
	}

	@Override
	protected long getPeriod() {
		return getTimeUnit().convert(24, TimeUnit.HOURS);
	}

	@Override
	protected TimeUnit getTimeUnit() {
		return TimeUnit.MINUTES;
	}

}
