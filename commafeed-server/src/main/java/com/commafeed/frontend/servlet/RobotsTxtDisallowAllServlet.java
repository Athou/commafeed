package com.commafeed.frontend.servlet;

import org.apache.hc.core5.http.HttpStatus;

import com.commafeed.CommaFeedConfiguration;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@Path("/robots.txt")
@PermitAll
@Produces(MediaType.TEXT_PLAIN)
@RequiredArgsConstructor
@Singleton
public class RobotsTxtDisallowAllServlet {

	private final CommaFeedConfiguration config;

	@GET
	public Response get() {
		if (config.hideFromWebCrawlers()) {
			return Response.ok("User-agent: *\nDisallow: /").build();
		} else {
			return Response.status(HttpStatus.SC_NOT_FOUND).build();
		}
	}
}