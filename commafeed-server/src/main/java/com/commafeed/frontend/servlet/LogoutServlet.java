package com.commafeed.frontend.servlet;

import java.net.URI;
import java.time.Instant;
import java.util.Date;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.commafeed.CommaFeedConfiguration;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Path("/logout")
@PermitAll
@Singleton
public class LogoutServlet {

	private final CommaFeedConfiguration config;
	private final String cookieName;

	public LogoutServlet(CommaFeedConfiguration config, @ConfigProperty(name = "quarkus.http.auth.form.cookie-name") String cookieName) {
		this.config = config;
		this.cookieName = cookieName;
	}

	@GET
	public Response get() {
		NewCookie removeCookie = new NewCookie.Builder(cookieName).maxAge(0).expiry(Date.from(Instant.EPOCH)).path("/").build();
		return Response.temporaryRedirect(URI.create(config.publicUrl())).cookie(removeCookie).build();
	}
}
