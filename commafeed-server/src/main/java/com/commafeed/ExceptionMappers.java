package com.commafeed;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.Status;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import io.quarkus.security.AuthenticationFailedException;
import jakarta.annotation.Priority;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(1)
public class ExceptionMappers {

	// display a message when the user fails to authenticate
	@ServerExceptionMapper(AuthenticationFailedException.class)
	public RestResponse<AuthenticationExceptionInfo> authenticationFailed(AuthenticationFailedException e) {
		return RestResponse.status(RestResponse.Status.UNAUTHORIZED, new AuthenticationExceptionInfo(e.getMessage()));
	}

	// display a message for validation errors
	@ServerExceptionMapper(ValidationException.class)
	public RestResponse<ValidationExceptionInfo> validationException(ValidationException e) {
		return RestResponse.status(Status.BAD_REQUEST, new ValidationExceptionInfo(e.getMessage()));
	}

	public record AuthenticationExceptionInfo(String message) {
	}

	public record ValidationExceptionInfo(String message) {
	}
}
