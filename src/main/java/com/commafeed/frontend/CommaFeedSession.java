package com.commafeed.frontend;

import java.util.Set;

import javax.inject.Inject;

import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

import com.commafeed.backend.dao.UserRoleService;
import com.commafeed.backend.dao.UserService;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class CommaFeedSession extends AuthenticatedWebSession {

	@Inject
	UserService userService;

	@Inject
	UserRoleService userRoleService;

	private User user;
	private Roles roles = new Roles();

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
		return roles;
	}

	@Override
	public boolean authenticate(String userName, String password) {
		User user = userService.login(userName, password);
		if (user == null) {
			this.user = null;
			this.roles = new Roles();
		} else {

			Set<String> roleSet = Sets.newHashSet();
			for (Role role : userRoleService.getRoles(user)) {
				roleSet.add(role.name());
			}
			this.user = user;
			this.roles = new Roles(roleSet.toArray(new String[0]));
		}
		return user != null;
	}

}
