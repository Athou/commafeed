package com.commafeed.integration;

import java.net.HttpCookie;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.HttpHeaders;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.commafeed.ExceptionMappers.UnauthorizedResponse;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.frontend.model.request.ProfileModificationRequest;
import com.commafeed.frontend.model.request.SubscribeRequest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
class SecurityIT extends BaseIT {

	@Test
	void notLoggedIn() {
		UnauthorizedResponse info = RestAssured.given()
				.get("rest/user/profile")
				.then()
				.statusCode(HttpStatus.SC_UNAUTHORIZED)
				.extract()
				.as(UnauthorizedResponse.class);
		Assertions.assertTrue(info.allowRegistrations());
	}

	@Test
	void formLogin() {
		List<HttpCookie> cookies = login();
		cookies.forEach(c -> Assertions.assertTrue(c.getMaxAge() > 0));

		RestAssured.given()
				.header(HttpHeaders.COOKIE, cookies.stream().map(HttpCookie::toString).collect(Collectors.joining(";")))
				.get("rest/user/profile")
				.then()
				.statusCode(HttpStatus.SC_OK);
	}

	@Test
	void basicAuthLogin() {
		RestAssured.given().auth().preemptive().basic("admin", "admin").get("rest/user/profile").then().statusCode(HttpStatus.SC_OK);
	}

	@Test
	void wrongPassword() {
		RestAssured.given()
				.auth()
				.preemptive()
				.basic("admin", "wrong-password")
				.get("rest/user/profile")
				.then()
				.statusCode(HttpStatus.SC_UNAUTHORIZED);
	}

	@Test
	void missingRole() {
		RestAssured.given().auth().preemptive().basic("demo", "demo").get("rest/admin/metrics").then().statusCode(HttpStatus.SC_FORBIDDEN);
	}

	@Test
	void apiKey() {
		// create api key
		ProfileModificationRequest req = new ProfileModificationRequest();
		req.setCurrentPassword("admin");
		req.setNewApiKey(true);
		RestAssured.given()
				.auth()
				.preemptive()
				.basic("admin", "admin")
				.body(req)
				.contentType(ContentType.JSON)
				.post("rest/user/profile")
				.then()
				.statusCode(HttpStatus.SC_OK);

		// fetch api key
		String apiKey = RestAssured.given()
				.auth()
				.preemptive()
				.basic("admin", "admin")
				.get("rest/user/profile")
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.as(UserModel.class)
				.getApiKey();

		// subscribe to a feed
		SubscribeRequest subscribeRequest = new SubscribeRequest();
		subscribeRequest.setUrl(getFeedUrl());
		subscribeRequest.setTitle("my title for this feed");
		long subscriptionId = RestAssured.given()
				.auth()
				.preemptive()
				.basic("admin", "admin")
				.body(subscribeRequest)
				.contentType(ContentType.JSON)
				.post("rest/feed/subscribe")
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.as(Long.class);

		// get entries with api key
		Entries entries = RestAssured.given()
				.queryParam("id", subscriptionId)
				.queryParam("readType", "unread")
				.queryParam("apiKey", apiKey)
				.get("rest/feed/entries")
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.as(Entries.class);
		Assertions.assertEquals("my title for this feed", entries.getName());

		// mark entry as read and expect it won't work because it's not a GET request
		MarkRequest markRequest = new MarkRequest();
		markRequest.setId("1");
		markRequest.setRead(true);
		RestAssured.given()
				.body(markRequest)
				.contentType(ContentType.JSON)
				.queryParam("apiKey", apiKey)
				.post("rest/entry/mark")
				.then()
				.statusCode(HttpStatus.SC_UNAUTHORIZED);
	}
}
