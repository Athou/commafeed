package com.commafeed.frontend.servlet;

import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.UserSettings;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class CustomJsServlet extends AbstractCustomCodeServlet {

	private static final long serialVersionUID = 1L;

	@Inject
	public CustomJsServlet(UnitOfWork unitOfWork, UserDAO userDAO, UserSettingsDAO userSettingsDAO) {
		super(unitOfWork, userDAO, userSettingsDAO);
	}

	@Override
	protected String getMimeType() {
		return "application/javascript";
	}

	@Override
	protected String getCustomCode(UserSettings settings) {
		return settings.getCustomJs();
	}

}
