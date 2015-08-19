package com.commafeed.frontend.session;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;

public class SessionHelperFactory extends AbstractContainerRequestValueFactory<SessionHelper> {

	@Context
	HttpServletRequest request;

	@Override
	public SessionHelper provide() {
		return new SessionHelper(request);
	}
}