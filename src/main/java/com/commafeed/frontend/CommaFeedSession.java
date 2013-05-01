package com.commafeed.frontend;

import java.util.Set;

import javax.inject.Inject;

import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.services.UserService;
import com.google.common.collect.Sets;

public class CommaFeedSession extends AuthenticatedWebSession {

	private static final long serialVersionUID = 1L;

	@Inject
	UserService userService;

	@Inject
	UserRoleDAO userRoleDAO;

	private User user;
	private Roles roles = new Roles();

	public CommaFeedSession(Request request) {
		super(request);
	}

	public User getUser() {
		return user;
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
		setUser(user);
		return user != null;
	}

	public void setUser(User user) {
		if (user == null) {
			this.user = null;
			this.roles = new Roles();
		} else {

			Set<String> roleSet = Sets.newHashSet();
			for (Role role : userRoleDAO.findRoles(user)) {
				roleSet.add(role.name());
			}
			this.user = user;
			this.roles = new Roles(roleSet.toArray(new String[0]));
		}
	}

}
