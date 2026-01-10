package com.commafeed.frontend.servlet;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.Operation;

import com.commafeed.security.CookieService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Path("/logout")
@PermitAll
@Singleton
public class LogoutServlet {

	private final UriInfo uri;
	private final CookieService cookieService;

	@GET
	@Operation(hidden = true)
	public Response get() {
		NewCookie removeCookie = cookieService.buildLogoutCookie();
		return Response.temporaryRedirect(uri.getBaseUri()).cookie(removeCookie).build();
	}
}
