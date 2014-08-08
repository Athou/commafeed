package com.commafeed;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import lombok.RequiredArgsConstructor;

import com.commafeed.backend.model.User;
import com.commafeed.backend.service.UserService;
import com.google.common.base.Optional;

@RequiredArgsConstructor
public class CommaFeedAuthenticator implements Authenticator<BasicCredentials, User> {

	private final UserService userService;

	@Override
	public Optional<User> authenticate(final BasicCredentials credentials) throws AuthenticationException {
		return Optional.fromNullable(userService.login(credentials.getUsername(), credentials.getPassword()));
	}
}
