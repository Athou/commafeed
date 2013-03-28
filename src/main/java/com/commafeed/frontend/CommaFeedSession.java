package com.commafeed.frontend;

import javax.inject.Inject;

import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

import com.commafeed.backend.dao.UserService;
import com.commafeed.backend.model.User;
import com.commafeed.backend.security.Role;

@SuppressWarnings("serial")
public class CommaFeedSession extends AuthenticatedWebSession {

	@Inject
	UserService userService;

	private User user;

	public CommaFeedSession(Request request) {
		super(request);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public static CommaFeedSession get() {
		return (CommaFeedSession) Session.get();
	}

	@Override
	public Roles getRoles() {
		// TODO change this
		return isSignedIn() ? new Roles(new String[] { Role.USER, Role.ADMIN })
				: new Roles();
	}

	@Override
	public boolean authenticate(String userName, String password) {
		User user = userService.login(userName, password);
		this.user = user;
		return user != null;
	}

}
