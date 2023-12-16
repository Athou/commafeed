package com.commafeed.integration.servlet;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.commafeed.integration.BaseIT;

class LogoutIT extends BaseIT {

	@Test
	void test() {
		String cookie = login();
		try (Response response = getClient().target(getBaseUrl() + "logout")
				.request()
				.header(HttpHeaders.COOKIE, "JSESSIONID=" + cookie)
				.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
				.get()) {
			Assertions.assertEquals(HttpStatus.FOUND_302, response.getStatus());
		}
	}
}
