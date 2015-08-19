package com.commafeed.frontend.session;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import com.commafeed.backend.model.User;

@RequiredArgsConstructor()
public class SessionHelper {

	private static final String SESSION_KEY_USER = "user";

	private final HttpServletRequest request;

	public Optional<User> getLoggedInUser() {
		Optional<HttpSession> session = getSession(false);

		if (session.isPresent()) {
			User user = (User) session.get().getAttribute(SESSION_KEY_USER);
			return Optional.ofNullable(user);
		}

		return Optional.empty();
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
