package com.commafeed.integration.servlet;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.commafeed.frontend.model.Settings;
import com.commafeed.integration.BaseIT;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

class CustomCodeIT extends BaseIT {

	@Override
	protected JerseyClientBuilder configureClientBuilder(JerseyClientBuilder base) {
		return base.register(HttpAuthenticationFeature.basic("admin", "admin"));
	}

	@Test
	void test() {
		// get settings
		Settings settings = getClient().target(getApiBaseUrl() + "user/settings").request().get(Settings.class);

		// update settings
		settings.setCustomJs("custom-js");
		settings.setCustomCss("custom-css");
		getClient().target(getApiBaseUrl() + "user/settings").request().post(Entity.json(settings), Void.TYPE);

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
