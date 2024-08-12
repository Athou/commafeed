package com.commafeed.security.mechanism;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.vertx.http.runtime.FormAuthConfig;
import io.quarkus.vertx.http.runtime.FormAuthRuntimeConfig;
import io.quarkus.vertx.http.runtime.HttpBuildTimeConfig;
import io.quarkus.vertx.http.runtime.HttpConfiguration;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.FormAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.quarkus.vertx.http.runtime.security.PersistentLoginManager;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.impl.ServerCookie;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * HttpAuthenticationMechanism that wraps FormAuthenticationMechanism and sets a Max-Age on the cookie because it has no value by default.
 *
 * This is a workaround for https://github.com/quarkusio/quarkus/issues/42463
 */
@Priority(1)
@Singleton
@Slf4j
public class CookieMaxAgeFormAuthenticationMechanism implements HttpAuthenticationMechanism {

	// the temp encryption key, persistent across dev mode restarts
	static volatile String encryptionKey;

	private final FormAuthenticationMechanism delegate;

	public CookieMaxAgeFormAuthenticationMechanism(HttpConfiguration httpConfiguration, HttpBuildTimeConfig buildTimeConfig) {
		String key;
		if (httpConfiguration.encryptionKey.isEmpty()) {
			if (encryptionKey != null) {
				// persist across dev mode restarts
				key = encryptionKey;
			} else {
				byte[] data = new byte[32];
				new SecureRandom().nextBytes(data);
				key = encryptionKey = Base64.getEncoder().encodeToString(data);
				log.warn("Encryption key was not specified for persistent FORM auth, using temporary key {}", key);
			}
		} else {
			key = httpConfiguration.encryptionKey.get();
		}

		FormAuthConfig form = buildTimeConfig.auth.form;
		FormAuthRuntimeConfig runtimeForm = httpConfiguration.auth.form;
		String loginPage = startWithSlash(runtimeForm.loginPage.orElse(null));
		String errorPage = startWithSlash(runtimeForm.errorPage.orElse(null));
		String landingPage = startWithSlash(runtimeForm.landingPage.orElse(null));
		String postLocation = startWithSlash(form.postLocation);
		String usernameParameter = runtimeForm.usernameParameter;
		String passwordParameter = runtimeForm.passwordParameter;
		String locationCookie = runtimeForm.locationCookie;
		String cookiePath = runtimeForm.cookiePath.orElse(null);
		boolean redirectAfterLogin = landingPage != null;
		String cookieSameSite = runtimeForm.cookieSameSite.name();

		PersistentLoginManager loginManager = new PersistentLoginManager(key, runtimeForm.cookieName, runtimeForm.timeout.toMillis(),
				runtimeForm.newCookieInterval.toMillis(), runtimeForm.httpOnlyCookie, cookieSameSite, cookiePath) {
			@Override
			public void save(String value, RoutingContext context, String cookieName, RestoreResult restoreResult, boolean secureCookie) {
				super.save(value, context, cookieName, restoreResult, secureCookie);

				// add max age to the cookie
				Cookie cookie = context.request().getCookie(cookieName);
				if (cookie instanceof ServerCookie sc && sc.isChanged()) {
					cookie.setMaxAge(runtimeForm.timeout.toSeconds());
				}
			}
		};

		this.delegate = new FormAuthenticationMechanism(loginPage, postLocation, usernameParameter, passwordParameter, errorPage,
				landingPage, redirectAfterLogin, locationCookie, cookieSameSite, cookiePath, loginManager);
	}

	@Override
	public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
		return delegate.authenticate(context, identityProviderManager);
	}

	@Override
	public Uni<ChallengeData> getChallenge(RoutingContext context) {
		return delegate.getChallenge(context);
	}

	@Override
	public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
		return delegate.getCredentialTypes();
	}

	@Override
	public Uni<HttpCredentialTransport> getCredentialTransport(RoutingContext context) {
		return delegate.getCredentialTransport(context);
	}

	private static String startWithSlash(String page) {
		if (page == null) {
			return null;
		}
		return page.startsWith("/") ? page : "/" + page;
	}
}
