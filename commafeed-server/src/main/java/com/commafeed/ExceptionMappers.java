package com.commafeed;

import jakarta.annotation.Priority;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.Provider;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.RestResponse.Status;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import com.commafeed.security.CookieService;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.UnauthorizedException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Provider
@Priority(1)
public class ExceptionMappers {

	private final CookieService cookieService;
	private final CommaFeedConfiguration config;

	@ServerExceptionMapper(UnauthorizedException.class)
	public RestResponse<UnauthorizedResponse> unauthorized(UnauthorizedException e) {
		return RestResponse.status(Status.UNAUTHORIZED, new UnauthorizedResponse(e.getMessage(), config.users().allowRegistrations()));
	}

	@ServerExceptionMapper(AuthenticationFailedException.class)
	public RestResponse<AuthenticationFailed> authenticationFailed(AuthenticationFailedException e) {
		NewCookie logoutCookie = cookieService.buildLogoutCookie();
		return ResponseBuilder.create(Status.UNAUTHORIZED, new AuthenticationFailed(e.getMessage())).cookie(logoutCookie).build();
	}

	@ServerExceptionMapper(ValidationException.class)
	public RestResponse<ValidationFailed> validationFailed(ValidationException e) {
		return RestResponse.status(Status.BAD_REQUEST, new ValidationFailed(e.getMessage()));
	}

	@RegisterForReflection
	public record UnauthorizedResponse(String message, boolean allowRegistrations) {
	}

	@RegisterForReflection
	public record AuthenticationFailed(String message) {
	}

	@RegisterForReflection
	public record ValidationFailed(String message) {
	}
}
