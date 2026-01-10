package com.commafeed.security;

import java.time.Instant;
import java.util.Date;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.NewCookie;

import io.quarkus.vertx.http.runtime.VertxHttpConfig;

@Singleton
public class CookieService {

	private final String cookieName;

	public CookieService(VertxHttpConfig config) {
		this.cookieName = config.auth().form().cookieName();
	}

	public NewCookie buildLogoutCookie() {
		return new NewCookie.Builder(cookieName).maxAge(0).expiry(Date.from(Instant.EPOCH)).path("/").build();
	}

}
