package com.commafeed.frontend.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.hibernate.SessionFactory;

import com.commafeed.CommaFeedApplication;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;

@SuppressWarnings("serial")
@RequiredArgsConstructor
public class CustomCssServlet extends HttpServlet {

	private final SessionFactory sessionFactory;
	private final UserSettingsDAO userSettingsDAO;
	private final CommaFeedConfiguration config;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final User user = (User) req.getSession().getAttribute(CommaFeedApplication.SESSION_USER);
		if (user == null) {
			resp.sendRedirect(resp.encodeRedirectURL(config.getApplicationSettings().getPublicUrl()));
			return;
		}

		UserSettings settings = new UnitOfWork<UserSettings>(sessionFactory) {
			@Override
			protected UserSettings runInSession() {
				return userSettingsDAO.findByUser(user);
			}
		}.run();

		resp.setContentType("text/css");
		resp.getWriter().write(settings.getCustomCss());
	}
}
