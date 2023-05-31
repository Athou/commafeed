package com.commafeed.frontend.session;

import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

@Singleton
public class SessionHelperFactoryProvider extends AbstractValueParamProvider {

	private final HttpServletRequest request;

	@Inject
	public SessionHelperFactoryProvider(final MultivaluedParameterExtractorProvider extractorProvider, HttpServletRequest request) {
		super(() -> extractorProvider, Parameter.Source.CONTEXT);
		this.request = request;
	}

	@Override
	protected Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {
		final Class<?> classType = parameter.getRawType();

		Context context = parameter.getAnnotation(Context.class);
		if (context == null) {
			return null;
		}

		if (!classType.isAssignableFrom(SessionHelper.class)) {
			return null;
		}

		return r -> new SessionHelper(request);
	}

	public static class Binder extends AbstractBinder {

		@Override
		protected void configure() {
			bind(SessionHelperFactoryProvider.class).to(ValueParamProvider.class).in(Singleton.class);
		}
	}
}
