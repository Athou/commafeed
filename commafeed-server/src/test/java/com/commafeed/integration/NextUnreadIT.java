package com.commafeed.integration;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.awaitility.Awaitility;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NextUnreadIT extends BaseIT {

	@Test
	void test() {
		Long subscriptionId = subscribe(getFeedUrl());
		Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> !getFeedEntries(subscriptionId).getEntries().isEmpty());

		String cookie = login();
		Response response = getClient().target(getBaseUrl() + "next")
				.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
				.request()
				.header(HttpHeaders.COOKIE, "JSESSIONID=" + cookie)
				.get();
		Assertions.assertEquals(HttpStatus.FOUND_302, response.getStatus());
		Assertions.assertEquals("https://www.commafeed.com/2", response.getHeaderString(HttpHeaders.LOCATION));
	}

}
