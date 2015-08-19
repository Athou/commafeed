package com.commafeed.frontend.servlet;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.SessionFactory;

import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.frontend.session.SessionHelper;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("serial")
@RequiredArgsConstructor(onConstructor = @__({ @Inject }) )
@Singleton
public class CustomCssServlet extends HttpServlet {

	private final SessionFactory sessionFactory;
	private final UserSettingsDAO userSettingsDAO;

	@Override
	protected void doGet(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/css");

		final Optional<User> user = new SessionHelper(req).getLoggedInUser();
		if (!user.isPresent()) {
			return;
		}

		UserSettings settings = UnitOfWork.call(sessionFactory, () -> userSettingsDAO.findByUser(user.get()));
		if (settings == null || settings.getCustomCss() == null) {
			return;
		}

		resp.getWriter().write(settings.getCustomCss());
	}
}
