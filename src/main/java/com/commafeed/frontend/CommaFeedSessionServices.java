package com.commafeed.frontend;

import javax.inject.Inject;

import org.apache.wicket.Component;

import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.services.UserService;

// extend Component in order to benefit from injection 
public class CommaFeedSessionServices extends Component {

	@Inject
	UserService userService;

	@Inject
	UserRoleDAO userRoleDAO;

	public CommaFeedSessionServices() {
		super("services");
	}

	public UserService getUserService() {
		return userService;
	}

	public UserRoleDAO getUserRoleDAO() {
		return userRoleDAO;
	}

	@Override
	protected void onRender() {
		// do nothing
	}
}
