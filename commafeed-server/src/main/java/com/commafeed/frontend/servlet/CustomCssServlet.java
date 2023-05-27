package com.commafeed.frontend.servlet;

import javax.inject.Inject;

import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.UserSettings;

public class CustomCssServlet extends AbstractCustomCodeServlet {

	private static final long serialVersionUID = 1L;

	@Inject
	public CustomCssServlet(UnitOfWork unitOfWork, UserSettingsDAO userSettingsDAO) {
		super(unitOfWork, userSettingsDAO);
	}

	@Override
	protected String getMimeType() {
		return "text/css";
	}

	@Override
	protected String getCustomCode(UserSettings settings) {
		return settings.getCustomCss();
	}

}
