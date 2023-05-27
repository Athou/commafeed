package com.commafeed.frontend.servlet;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.UserSettings;

@Singleton
public class CustomJsServlet extends AbstractCustomCodeServlet {

	private static final long serialVersionUID = 1L;

	@Inject
	public CustomJsServlet(UnitOfWork unitOfWork, UserSettingsDAO userSettingsDAO) {
		super(unitOfWork, userSettingsDAO);
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
