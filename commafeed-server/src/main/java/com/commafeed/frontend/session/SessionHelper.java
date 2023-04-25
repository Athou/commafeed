package com.commafeed.frontend.session;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.commafeed.backend.model.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SessionHelper {

	private static final String SESSION_KEY_USER = "user";

	private final HttpServletRequest request;

	public Optional<User> getLoggedInUser() {
		Optional<HttpSession> session = getSession(false);
		if (session.isPresent()) {
			return getLoggedInUser(session.get());
		}

		return Optional.empty();
	}

	public static Optional<User> getLoggedInUser(HttpSession session) {
		User user = (User) session.getAttribute(SESSION_KEY_USER);
		return Optional.ofNullable(user);
	}

	public void setLoggedInUser(User user) {
		Optional<HttpSession> session = getSession(true);
		session.get().setAttribute(SESSION_KEY_USER, user);
	}

	private Optional<HttpSession> getSession(boolean force) {
		HttpSession session = request.getSession(force);
		return Optional.ofNullable(session);
	}

}
