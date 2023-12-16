package com.commafeed.integration.servlet;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.commafeed.integration.BaseIT;

class NextUnreadIT extends BaseIT {

	@Test
	void test() {
		subscribeAndWaitForEntries(getFeedUrl());

		String cookie = login();
		Response response = getClient().target(getBaseUrl() + "next")
				.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
				.request()
				.header(HttpHeaders.COOKIE, "JSESSIONID=" + cookie)
				.get();
		Assertions.assertEquals(HttpStatus.FOUND_302, response.getStatus());
		Assertions.assertEquals("https://hostname.local/commafeed/2", response.getHeaderString(HttpHeaders.LOCATION));
	}

}
