package com.commafeed.security;

import io.quarkus.vertx.http.runtime.HttpConfiguration;
import io.quarkus.vertx.http.runtime.security.FormAuthenticationMechanism;
import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.impl.ServerCookie;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

/**
 * Intercepts responses and sets a Max-Age on the cookie created by {@link FormAuthenticationMechanism} because it has no value by default.
 *
 * This is a workaround for https://github.com/quarkusio/quarkus/issues/42463
 */
@RequiredArgsConstructor
@Singleton
public class FormAuthenticationCookieInterceptor {

	private final HttpConfiguration config;

	@RouteFilter(Integer.MAX_VALUE)
	public void cookieInterceptor(RoutingContext context) {
		context.addHeadersEndHandler(v -> {
			Cookie cookie = context.request().getCookie(config.auth.form.cookieName);
			if (cookie instanceof ServerCookie sc && sc.isChanged()) {
				cookie.setMaxAge(config.auth.form.timeout.toSeconds());
			}
		});

		context.next();
	}
}
