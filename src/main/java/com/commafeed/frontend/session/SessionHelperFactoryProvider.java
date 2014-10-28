package com.commafeed.frontend.session;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Context;

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

@Singleton
public class SessionHelperFactoryProvider extends AbstractValueFactoryProvider {

	@Inject
	public SessionHelperFactoryProvider(final MultivaluedParameterExtractorProvider extractorProvider, final ServiceLocator injector) {
		super(extractorProvider, injector, Parameter.Source.CONTEXT);
	}

	@Override
	protected Factory<?> createValueFactory(final Parameter parameter) {
		final Class<?> classType = parameter.getRawType();

		Context context = parameter.getAnnotation(Context.class);
		if (context == null)
			return null;

		if (classType.isAssignableFrom(SessionHelper.class)) {
			return new SessionHelperFactory();
		} else {
			return null;
		}
	}

	public static class SessionHelperInjectionResolver extends ParamInjectionResolver<Context> {
		public SessionHelperInjectionResolver() {
			super(SessionHelperFactoryProvider.class);
		}
	}

	public static class Binder extends AbstractBinder {

		@Override
		protected void configure() {
			bind(SessionHelperFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
			bind(SessionHelperInjectionResolver.class).to(new TypeLiteral<InjectionResolver<Context>>() {
			}).in(Singleton.class);
		}
	}
}
