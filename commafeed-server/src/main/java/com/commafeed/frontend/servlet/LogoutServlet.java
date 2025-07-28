package com.commafeed.frontend.servlet;

import java.time.Instant;
import java.util.Date;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/logout")
@PermitAll
@Singleton
public class LogoutServlet {

	private final UriInfo uri;
	private final String cookieName;

	public LogoutServlet(UriInfo uri, @ConfigProperty(name = "quarkus.http.auth.form.cookie-name") String cookieName) {
		this.uri = uri;
		this.cookieName = cookieName;
	}

	@GET
	@Operation(hidden = true)
	public Response get() {
		NewCookie removeCookie = new NewCookie.Builder(cookieName).maxAge(0).expiry(Date.from(Instant.EPOCH)).path("/").build();
		return Response.temporaryRedirect(uri.getBaseUri()).cookie(removeCookie).build();
	}
}
