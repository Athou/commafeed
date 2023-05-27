package com.commafeed.frontend.servlet;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.frontend.session.SessionHelper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class AbstractCustomCodeServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final UnitOfWork unitOfWork;
	private final UserSettingsDAO userSettingsDAO;

	@Override
	protected final void doGet(final HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType(getMimeType());

		final Optional<User> user = new SessionHelper(req).getLoggedInUser();
		if (!user.isPresent()) {
			return;
		}

		UserSettings settings = unitOfWork.call(() -> userSettingsDAO.findByUser(user.get()));
		if (settings == null) {
			return;
		}

		String customCode = getCustomCode(settings);
		if (customCode == null) {
			return;
		}

		resp.getWriter().write(customCode);
	}

	protected abstract String getMimeType();

	protected abstract String getCustomCode(UserSettings settings);
}
