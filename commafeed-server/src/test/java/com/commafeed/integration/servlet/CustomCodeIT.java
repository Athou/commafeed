package com.commafeed.integration.servlet;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.commafeed.frontend.model.Settings;
import com.commafeed.integration.BaseIT;

class CustomCodeIT extends BaseIT {

	@Test
	void test() {
		// get settings
		Settings settings = null;
		try (Response response = getClient().target(getApiBaseUrl() + "user/settings").request().get()) {
			settings = response.readEntity(Settings.class);
		}

		// update settings
		settings.setCustomJs("custom-js");
		settings.setCustomCss("custom-css");
		try (Response response = getClient().target(getApiBaseUrl() + "user/settings").request().post(Entity.json(settings))) {
			Assertions.assertEquals(HttpStatus.OK_200, response.getStatus());
		}

		// check custom code servlets
		String cookie = login();
		try (Response response = getClient().target(getBaseUrl() + "custom_js.js")
				.request()
				.header(HttpHeaders.COOKIE, "JSESSIONID=" + cookie)
				.get()) {
			Assertions.assertEquals("custom-js", response.readEntity(String.class));
		}
		try (Response response = getClient().target(getBaseUrl() + "custom_css.css")
				.request()
				.header(HttpHeaders.COOKIE, "JSESSIONID=" + cookie)
				.get()) {
			Assertions.assertEquals("custom-css", response.readEntity(String.class));
		}
	}
}
