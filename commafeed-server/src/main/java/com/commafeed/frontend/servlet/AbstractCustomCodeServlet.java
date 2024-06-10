package com.commafeed.frontend.servlet;

import java.io.IOException;
import java.util.Optional;

import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.frontend.session.SessionHelper;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class AbstractCustomCodeServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final transient UnitOfWork unitOfWork;
	private final transient UserDAO userDAO;
	private final transient UserSettingsDAO userSettingsDAO;

	@Override
	protected final void doGet(final HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType(getMimeType());

		SessionHelper sessionHelper = new SessionHelper(req);
		Optional<Long> userId = sessionHelper.getLoggedInUserId();
		final Optional<User> user = unitOfWork.call(() -> userId.map(userDAO::findById));
		if (user.isEmpty()) {
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
