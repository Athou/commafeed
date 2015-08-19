package com.commafeed.frontend.auth;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.RequiredArgsConstructor;

import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.session.SessionHelper;

@RequiredArgsConstructor
public class SecurityCheckFactory extends AbstractContainerRequestValueFactory<User> {

	private static final String PREFIX = "Basic";

	@Context
	HttpServletRequest request;

	@Inject
	UserService userService;

	private final Role role;
	private final boolean apiKeyAllowed;

	@Override
	public User provide() {
		Optional<User> user = apiKeyLogin();
		if (!user.isPresent()) {
			user = basicAuthenticationLogin();
		}
		if (!user.isPresent()) {
			user = cookieSessionLogin(new SessionHelper(request));
		}

		if (user.isPresent()) {
			Set<Role> roles = userService.getRoles(user.get());
			if (roles.contains(role)) {
				return user.get();
			} else {
				throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
						.entity("You don't have the required role to access this resource.").type(MediaType.TEXT_PLAIN_TYPE).build());
			}
		} else {
			throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
					.entity("Credentials are required to access this resource.").type(MediaType.TEXT_PLAIN_TYPE).build());
		}
	}

	Optional<User> cookieSessionLogin(SessionHelper sessionHelper) {
		Optional<User> loggedInUser = sessionHelper.getLoggedInUser();
		if (loggedInUser.isPresent()) {
			userService.performPostLoginActivities(loggedInUser.get());
		}
		return loggedInUser;
	}

	private Optional<User> basicAuthenticationLogin() {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null) {
			int space = header.indexOf(' ');
			if (space > 0) {
				String method = header.substring(0, space);
				if (PREFIX.equalsIgnoreCase(method)) {
					String decoded = B64Code.decode(header.substring(space + 1), StringUtil.__ISO_8859_1);
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

}
