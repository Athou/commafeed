package com.commafeed.frontend.auth;

import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import com.commafeed.backend.model.User;
import com.commafeed.backend.service.UserService;

import lombok.RequiredArgsConstructor;

@Singleton
public class SecurityCheckFactoryProvider extends AbstractValueParamProvider {

	private final UserService userService;
	private final HttpServletRequest request;

	@Inject
	public SecurityCheckFactoryProvider(final MultivaluedParameterExtractorProvider extractorProvider, UserService userService,
			HttpServletRequest request) {
		super(() -> extractorProvider, Parameter.Source.UNKNOWN);
		this.userService = userService;
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

		return new SecurityCheckFactory(userService, request, securityCheck.value(), securityCheck.apiKeyAllowed());
	}

	@RequiredArgsConstructor
	public static class Binder extends AbstractBinder {

		private final UserService userService;

		@Override
		protected void configure() {
			bind(SecurityCheckFactoryProvider.class).to(ValueParamProvider.class).in(Singleton.class);
			bind(userService).to(UserService.class);
		}
	}

}
