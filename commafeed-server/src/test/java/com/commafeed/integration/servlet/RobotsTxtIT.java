package com.commafeed.integration.servlet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.commafeed.integration.BaseIT;

import jakarta.ws.rs.core.Response;

class RobotsTxtIT extends BaseIT {
	@Test
	void test() {
		try (Response response = getClient().target(getBaseUrl() + "robots.txt").request().get()) {
			Assertions.assertEquals("User-agent: *\nDisallow: /", response.readEntity(String.class));
		}
	}
}
