package com.commafeed.frontend.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.hibernate.SessionFactory;

import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.service.UserService;
import com.google.common.base.Optional;

@SuppressWarnings("serial")
@RequiredArgsConstructor
public class CustomCssServlet extends HttpServlet {

	private final SessionFactory sessionFactory;
	private final UserSettingsDAO userSettingsDAO;
	private final UserService userService;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/css");

		final Optional<User> user = userService.login(req.getSession());
		if (!user.isPresent()) {
			return;
		}

		UserSettings settings = new UnitOfWork<UserSettings>(sessionFactory) {
			@Override
			protected UserSettings runInSession() {
				return userSettingsDAO.findByUser(user.get());
			}
		}.run();

		if (settings == null || settings.getCustomCss() == null) {
			return;
		}

		resp.getWriter().write(settings.getCustomCss());
	}
}
