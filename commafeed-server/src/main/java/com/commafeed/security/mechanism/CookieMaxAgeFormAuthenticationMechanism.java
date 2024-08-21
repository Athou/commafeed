package com.commafeed.security.mechanism;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.HttpConfiguration;
import io.quarkus.vertx.http.runtime.security.FormAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.impl.ServerCookie;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

/**
 * HttpAuthenticationMechanism that wraps FormAuthenticationMechanism and sets a Max-Age on the cookie because it has no value by default.
 *
 * This is a workaround for https://github.com/quarkusio/quarkus/issues/42463
 */
@Priority(1)
@RequiredArgsConstructor
@Singleton
@Slf4j
public class CookieMaxAgeFormAuthenticationMechanism implements HttpAuthenticationMechanism {

	@Delegate
	private final FormAuthenticationMechanism delegate;
	private final HttpConfiguration config;

	@Override
	public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
		context.addHeadersEndHandler(v -> {
			Cookie cookie = context.request().getCookie(config.auth.form.cookieName);
			if (cookie instanceof ServerCookie sc && sc.isChanged()) {
				cookie.setMaxAge(config.auth.form.timeout.toSeconds());
			}
		});

		return delegate.authenticate(context, identityProviderManager);
	}
}
