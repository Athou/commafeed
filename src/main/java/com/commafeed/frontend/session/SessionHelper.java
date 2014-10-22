package com.commafeed.frontend.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import com.commafeed.backend.model.User;
import com.google.common.base.Optional;

@RequiredArgsConstructor()
public class SessionHelper {
	
	private static final String SESSION_KEY_USER = "user";
	
	private final HttpServletRequest request;
	
	public Optional<User> getLoggedInUser() {
		Optional<HttpSession> session = getSession(false);
		
		if (session.isPresent()) {
			User user = (User) session.get().getAttribute(SESSION_KEY_USER);
			return Optional.fromNullable(user);
		}
		
		return Optional.absent();
	}
	
	public void setLoggedInUser(User user) {
		Optional<HttpSession> session = getSession(true);
		session.get().setAttribute(SESSION_KEY_USER, user);
	}
	
	private Optional<HttpSession> getSession(boolean force) {
		HttpSession session = request.getSession(force);
		return Optional.fromNullable(session);
	}

}
