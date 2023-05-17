package com.commafeed.frontend.servlet;

import javax.inject.Inject;

import org.hibernate.SessionFactory;

import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.UserSettings;

public class CustomCssServlet extends AbstractCustomCodeServlet {

	private static final long serialVersionUID = 1L;

	@Inject
	public CustomCssServlet(SessionFactory sessionFactory, UserSettingsDAO userSettingsDAO) {
		super(sessionFactory, userSettingsDAO);
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
