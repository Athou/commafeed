package com.commafeed.backend.service.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import jakarta.inject.Singleton;

import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Singleton
public class PostLoginActivities {

	private final UserDAO userDAO;
	private final UnitOfWork unitOfWork;

	public void executeFor(User user) {
		// only update lastLogin every once in a while in order to avoid invalidating the cache every time someone logs in
		Instant now = Instant.now();
		Instant lastLogin = user.getLastLogin();
		if (lastLogin == null || ChronoUnit.MINUTES.between(lastLogin, now) >= 30) {
			user.setLastLogin(now);
			unitOfWork.run(() -> userDAO.merge(user));
		}
	}
}
