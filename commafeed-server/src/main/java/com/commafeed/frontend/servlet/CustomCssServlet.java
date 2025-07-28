package com.commafeed.frontend.servlet;

import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import org.eclipse.microprofile.openapi.annotations.Operation;

import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.security.AuthenticationContext;

import lombok.RequiredArgsConstructor;

@Path("/custom_css.css")
@Produces("text/css")
@RequiredArgsConstructor
@Singleton
public class CustomCssServlet {

	private final AuthenticationContext authenticationContext;
	private final UserSettingsDAO userSettingsDAO;

	@GET
	@Transactional
	@Operation(hidden = true)
	public String get() {
		User user = authenticationContext.getCurrentUser();
		if (user == null) {
			return "";
		}

		UserSettings settings = userSettingsDAO.findByUser(user);
		if (settings == null) {
			return "";
		}

		return settings.getCustomCss();
	}

}
