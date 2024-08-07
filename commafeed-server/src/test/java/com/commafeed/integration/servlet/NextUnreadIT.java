package com.commafeed.integration.servlet;

import java.net.HttpCookie;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.HttpStatus;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

@QuarkusTest
class NextUnreadIT extends BaseIT {

	@Override
	protected JerseyClientBuilder configureClientBuilder(JerseyClientBuilder base) {
		return base.register(HttpAuthenticationFeature.basic("admin", "admin"));
	}

	@Test
	void test() {
		subscribeAndWaitForEntries(getFeedUrl());

		List<HttpCookie> cookies = login();
		Response response = getClient().target(getBaseUrl() + "next")
				.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
				.request()
				.header(HttpHeaders.COOKIE, cookies.stream().map(HttpCookie::toString).collect(Collectors.joining(";")))
				.get();
		Assertions.assertEquals(HttpStatus.SC_TEMPORARY_REDIRECT, response.getStatus());
		Assertions.assertEquals("https://hostname.local/commafeed/2", response.getHeaderString(HttpHeaders.LOCATION));
	}

}
