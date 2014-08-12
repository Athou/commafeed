package com.commafeed.frontend.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.hibernate.SessionFactory;

import com.commafeed.CommaFeedApplication;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;

@SuppressWarnings("serial")
@RequiredArgsConstructor
public class CustomCssServlet extends HttpServlet {

	private final SessionFactory sessionFactory;
	private final UserSettingsDAO userSettingsDAO;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/css");

		final User user = (User) req.getSession().getAttribute(CommaFeedApplication.SESSION_USER);
		if (user == null) {
			return;
		}

		UserSettings settings = new UnitOfWork<UserSettings>(sessionFactory) {
			@Override
			protected UserSettings runInSession() {
				return userSettingsDAO.findByUser(user);
			}
		}.run();

		if (settings == null || settings.getCustomCss() == null) {
			return;
		}

		resp.getWriter().write(settings.getCustomCss());
	}
}
