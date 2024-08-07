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
import lombok.RequiredArgsConstructor;

@Path("/logout")
@PermitAll
@RequiredArgsConstructor
@Singleton
public class LogoutServlet {

	@ConfigProperty(name = "quarkus.http.auth.form.cookie-name")
	String cookieName;

	private final CommaFeedConfiguration config;

	@GET
	public Response get() {
		NewCookie removeCookie = new NewCookie.Builder(cookieName).maxAge(0).expiry(Date.from(Instant.EPOCH)).path("/").build();
		return Response.temporaryRedirect(URI.create(config.publicUrl())).cookie(removeCookie).build();
	}
}
