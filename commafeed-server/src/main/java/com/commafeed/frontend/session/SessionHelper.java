package com.commafeed.frontend.session;

import java.util.Optional;

import com.commafeed.backend.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SessionHelper {

	public static final String SESSION_KEY_USER_ID = "user-id";

	private final HttpServletRequest request;

	public Optional<Long> getLoggedInUserId() {
		HttpSession session = request.getSession(false);
		return getLoggedInUserId(session);
	}

	public static Optional<Long> getLoggedInUserId(HttpSession session) {
		if (session == null) {
			return Optional.empty();
		}
		Long userId = (Long) session.getAttribute(SESSION_KEY_USER_ID);
		return Optional.ofNullable(userId);
	}

	public void setLoggedInUser(User user) {
		request.getSession(true).setAttribute(SESSION_KEY_USER_ID, user.getId());
	}

}
