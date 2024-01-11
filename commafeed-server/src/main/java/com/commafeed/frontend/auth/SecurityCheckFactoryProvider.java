package com.commafeed.frontend.auth;

import java.util.function.Function;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.UserService;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Singleton
public class SecurityCheckFactoryProvider extends AbstractValueParamProvider {

	private final UserService userService;
	private final UserDAO userDAO;
	private final CommaFeedConfiguration config;
	private final HttpServletRequest request;

	@Inject
	public SecurityCheckFactoryProvider(final MultivaluedParameterExtractorProvider extractorProvider, UserDAO userDAO,
			UserService userService, CommaFeedConfiguration config, HttpServletRequest request) {
		super(() -> extractorProvider, Parameter.Source.UNKNOWN);
		this.userDAO = userDAO;
		this.userService = userService;
		this.config = config;
		this.request = request;
	}

	@Override
	protected Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {
		final Class<?> classType = parameter.getRawType();

		SecurityCheck securityCheck = parameter.getAnnotation(SecurityCheck.class);
		if (securityCheck == null) {
			return null;
		}

		if (!classType.isAssignableFrom(User.class)) {
			return null;
		}

		return new SecurityCheckFactory(userDAO, userService, config, request, securityCheck.value(), securityCheck.apiKeyAllowed());
	}

	@RequiredArgsConstructor
	public static class Binder extends AbstractBinder {

		private final UserDAO userDAO;
		private final UserService userService;
		private final CommaFeedConfiguration config;

		@Override
		protected void configure() {
			bind(SecurityCheckFactoryProvider.class).to(ValueParamProvider.class).in(Singleton.class);
			bind(userDAO).to(UserDAO.class);
			bind(userService).to(UserService.class);
			bind(config).to(CommaFeedConfiguration.class);
		}
	}

}
