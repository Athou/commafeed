package com.commafeed.frontend.auth;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.RequiredArgsConstructor;

import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.SessionHelper;
import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;

public class SecurityCheckProvider implements InjectableProvider<SecurityCheck, Parameter> {

	public static class SecurityCheckUserServiceProvider extends SingletonTypeInjectableProvider<Context, UserService> {

		public SecurityCheckUserServiceProvider(UserService userService) {
			super(UserService.class, userService);
		}
	}

	@RequiredArgsConstructor
	static class SecurityCheckInjectable extends AbstractHttpContextInjectable<User> {
		private static final String PREFIX = "Basic";

		private final HttpServletRequest request;
		private final UserService userService;
		private final Role role;
		private final boolean apiKeyAllowed;

		@Override
		public User getValue(HttpContext c) {
			Optional<User> user = apiKeyLogin(c);
			if (!user.isPresent()) {
				user = basicAuthenticationLogin(c);
			}
			if (!user.isPresent()) {
				user = cookieSessionLogin();
			}

			if (user.isPresent()) {
				if (user.get().hasRole(role)) {
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

		Optional<User> cookieSessionLogin() {
			SessionHelper sessionHelper = new SessionHelper(request);
			Optional<User> loggedInUser = sessionHelper.getLoggedInUser();
			if (loggedInUser.isPresent()) {
				userService.performPostLoginActivities(loggedInUser.get());
			}
			return loggedInUser;
		}

		private Optional<User> basicAuthenticationLogin(HttpContext c) {
			String header = c.getRequest().getHeaderValue(HttpHeaders.AUTHORIZATION);
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
			return Optional.absent();
		}

		private Optional<User> apiKeyLogin(HttpContext c) {
			String apiKey = c.getUriInfo().getQueryParameters().getFirst("apiKey");
			if (apiKey != null && apiKeyAllowed) {
				return userService.login(apiKey);
			}
			return Optional.absent();
		}
	}

	private HttpServletRequest request;
	private UserService userService;

	public SecurityCheckProvider(@Context HttpServletRequest request, @Context UserService userService) {
		this.request = request;
		this.userService = userService;
	}

	@Override
	public ComponentScope getScope() {
		return ComponentScope.PerRequest;
	}

	@Override
	public Injectable<?> getInjectable(ComponentContext ic, SecurityCheck sc, Parameter c) {
		return new SecurityCheckInjectable(request, userService, sc.value(), sc.apiKeyAllowed());
	}
}
