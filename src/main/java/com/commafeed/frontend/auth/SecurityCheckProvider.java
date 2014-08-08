package com.commafeed.frontend.auth;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.UserService;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

@Slf4j
public class SecurityCheckProvider implements InjectableProvider<SecurityCheck, Parameter> {

	private static class SecurityCheckInjectable<T> extends AbstractHttpContextInjectable<User> {
		private static final String PREFIX = "Basic";

		private final UserService userService;
		private Role role;
		private final boolean apiKeyAllowed;

		private SecurityCheckInjectable(UserService userService, Role role, boolean apiKeyAllowed) {
			this.userService = userService;
			this.role = role;
			this.apiKeyAllowed = apiKeyAllowed;
		}

		@Override
		public User getValue(HttpContext c) {
			final String header = c.getRequest().getHeaderValue(HttpHeaders.AUTHORIZATION);
			try {
				if (header != null) {
					final int space = header.indexOf(' ');
					if (space > 0) {
						final String method = header.substring(0, space);
						if (PREFIX.equalsIgnoreCase(method)) {
							final String decoded = B64Code.decode(header.substring(space + 1), StringUtil.__ISO_8859_1);
							final int i = decoded.indexOf(':');
							if (i > 0) {
								final String username = decoded.substring(0, i);
								final String password = decoded.substring(i + 1);
								final User user = userService.login(username, password);
								if (user != null && user.hasRole(role)) {
									return user;
								}
							}
						}
					}
				} else {
					String apiKey = c.getUriInfo().getPathParameters().getFirst("apiKey");
					if (apiKey != null && apiKeyAllowed) {
						User user = userService.login(apiKey);
						if (user != null && user.hasRole(role)) {
							return user;
						}
					}
				}
			} catch (IllegalArgumentException e) {
				log.debug("Error decoding credentials", e);
			}

			throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
					.header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"CommaFeed\"")
					.entity("Credentials are required to access this resource.").type(MediaType.TEXT_PLAIN_TYPE).build());
		}
	}

	private UserService userService;

	public SecurityCheckProvider(UserService userService) {
		this.userService = userService;
	}

	@Override
	public ComponentScope getScope() {
		return ComponentScope.PerRequest;
	}

	@Override
	public Injectable<?> getInjectable(ComponentContext ic, SecurityCheck sc, Parameter c) {
		return new SecurityCheckInjectable<>(userService, sc.value(), sc.apiKeyAllowed());
	}
}
