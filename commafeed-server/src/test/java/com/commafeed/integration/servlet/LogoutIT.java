package com.commafeed.integration.servlet;

import java.net.HttpCookie;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.HttpStatus;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

@QuarkusTest
class LogoutIT extends BaseIT {

	@Test
	void test() {
		List<HttpCookie> cookies = login();
		try (Response response = getClient().target(getBaseUrl() + "logout")
				.request()
				.header(HttpHeaders.COOKIE, cookies.stream().map(HttpCookie::toString).collect(Collectors.joining(";")))
				.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
				.get()) {
			Assertions.assertEquals(HttpStatus.SC_TEMPORARY_REDIRECT, response.getStatus());
			List<String> setCookieHeaders = response.getStringHeaders().get(HttpHeaders.SET_COOKIE);
			Assertions.assertTrue(setCookieHeaders.stream().flatMap(c -> HttpCookie.parse(c).stream()).allMatch(c -> c.getMaxAge() == 0));
		}
	}
}
