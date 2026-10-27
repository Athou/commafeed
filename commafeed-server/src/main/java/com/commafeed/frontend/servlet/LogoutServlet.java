package com.commafeed.frontend.servlet;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.security.AuthenticationContext;
import com.commafeed.security.CookieService;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;

import java.net.URI;

@RequiredArgsConstructor
@Path("/logout")
@PermitAll
@Singleton
public class LogoutServlet {

    private final UriInfo uri;
    private final CookieService cookieService;
    private final CommaFeedConfiguration config;
    private final AuthenticationContext authenticationContext;

    @GET
    @Operation(hidden = true)
    public Response get() {
        NewCookie removeCookie = cookieService.buildLogoutCookie();

        // users authenticated by the reverse proxy also need to be logged out of the single sign-on
        // provider, otherwise they would be logged in again right away. Users who logged in with a
        // password stay on CommaFeed.
        URI redirect =
                authenticationContext.isForwardAuthenticated()
                        ? config.forwardAuth().logoutUrl().map(URI::create).orElse(uri.getBaseUri())
                        : uri.getBaseUri();
        return Response.temporaryRedirect(redirect).cookie(removeCookie).build();
    }
}
