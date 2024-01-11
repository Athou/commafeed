package com.commafeed.frontend.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.glassfish.jersey.server.ContainerRequest;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.session.SessionHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SecurityCheckFactory implements Function<ContainerRequest, User> {

	private static final String PREFIX = "Basic";

	private final UserDAO userDAO;
	private final UserService userService;
	private final CommaFeedConfiguration config;
	private final HttpServletRequest request;
	private final Role role;
	private final boolean apiKeyAllowed;

	@Override
	public User apply(ContainerRequest req) {
		Optional<User> user = apiKeyLogin();
		if (user.isEmpty()) {
			user = basicAuthenticationLogin();
		}
		if (user.isEmpty()) {
			user = cookieSessionLogin(new SessionHelper(request));
		}

		if (user.isPresent()) {
			Set<Role> roles = userService.getRoles(user.get());
			if (roles.contains(role)) {
				return user.get();
			} else {
				throw buildWebApplicationException(Response.Status.FORBIDDEN, "You don't have the required role to access this resource.");
			}
		} else {
			throw buildWebApplicationException(Response.Status.UNAUTHORIZED, "Credentials are required to access this resource.");
		}
	}

	Optional<User> cookieSessionLogin(SessionHelper sessionHelper) {
		Optional<User> loggedInUser = sessionHelper.getLoggedInUserId().map(userDAO::findById);
		loggedInUser.ifPresent(userService::performPostLoginActivities);
		return loggedInUser;
	}

	private Optional<User> basicAuthenticationLogin() {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null) {
			int space = header.indexOf(' ');
			if (space > 0) {
				String method = header.substring(0, space);
				if (PREFIX.equalsIgnoreCase(method)) {
					byte[] decodedBytes = Base64.getDecoder().decode(header.substring(space + 1));
					String decoded = new String(decodedBytes, StandardCharsets.ISO_8859_1);
					int i = decoded.indexOf(':');
					if (i > 0) {
						String username = decoded.substring(0, i);
						String password = decoded.substring(i + 1);
						return userService.login(username, password);
					}
				}
			}
		}
		return Optional.empty();
	}

	private Optional<User> apiKeyLogin() {
		String apiKey = request.getParameter("apiKey");
		if (apiKey != null && apiKeyAllowed) {
			return userService.login(apiKey);
		}
		return Optional.empty();
	}

	private WebApplicationException buildWebApplicationException(Response.Status status, String message) {
		Map<String, Object> response = new HashMap<>();
		response.put("message", message);
		response.put("allowRegistrations", config.getApplicationSettings().getAllowRegistrations());
		return new WebApplicationException(Response.status(status).entity(response).type(MediaType.APPLICATION_JSON).build());
	}

}
