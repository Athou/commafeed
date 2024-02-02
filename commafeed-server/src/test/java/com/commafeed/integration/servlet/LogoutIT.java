package com.commafeed.integration.servlet;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.commafeed.CommaFeedDropwizardAppExtension;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.integration.BaseIT;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

class LogoutIT extends BaseIT {

	@Override
	protected CommaFeedDropwizardAppExtension buildExtension() {
		// override so we don't add http basic auth
		return new CommaFeedDropwizardAppExtension();
	}

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

		Builder req = getClient().target(getApiBaseUrl() + "user/profile").request().header(HttpHeaders.COOKIE, "JSESSIONID=" + cookie);
		Assertions.assertThrows(NotAuthorizedException.class, () -> req.get(UserModel.class));
	}
}
