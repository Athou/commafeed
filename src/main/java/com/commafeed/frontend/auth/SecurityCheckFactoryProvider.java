package com.commafeed.frontend.auth;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import com.commafeed.backend.model.User;
import com.commafeed.backend.service.UserService;

@Singleton
public class SecurityCheckFactoryProvider extends AbstractValueFactoryProvider {

	@Inject
	public SecurityCheckFactoryProvider(final MultivaluedParameterExtractorProvider extractorProvider, final ServiceLocator injector) {
		super(extractorProvider, injector, Parameter.Source.UNKNOWN);
	}

	@Override
	protected Factory<?> createValueFactory(final Parameter parameter) {
		final Class<?> classType = parameter.getRawType();

		SecurityCheck securityCheck = parameter.getAnnotation(SecurityCheck.class);
		if (securityCheck == null)
			return null;

		if (classType.isAssignableFrom(User.class)) {
			return new SecurityCheckFactory(securityCheck.value(), securityCheck.apiKeyAllowed());
		} else {
			return null;
		}
	}

	public static class SecurityCheckInjectionResolver extends ParamInjectionResolver<SecurityCheck> {
		public SecurityCheckInjectionResolver() {
			super(SecurityCheckFactoryProvider.class);
		}
	}

	@RequiredArgsConstructor
	public static class Binder extends AbstractBinder {

		private final UserService userService;

		@Override
		protected void configure() {
			bind(SecurityCheckFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
			bind(SecurityCheckInjectionResolver.class).to(new TypeLiteral<InjectionResolver<SecurityCheck>>() {
			}).in(Singleton.class);
			bind(userService).to(UserService.class);
		}
	}
}
