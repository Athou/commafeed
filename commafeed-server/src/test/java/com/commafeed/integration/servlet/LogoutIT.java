package com.commafeed.integration.servlet;

import java.net.HttpCookie;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import jakarta.ws.rs.core.HttpHeaders;

@QuarkusTest
class LogoutIT extends BaseIT {

	@Test
	void test() {
		List<HttpCookie> cookies = login();
		Headers responseHeaders = RestAssured.given()
				.header(HttpHeaders.COOKIE, cookies.stream().map(HttpCookie::toString).collect(Collectors.joining(";")))
				.redirects()
				.follow(false)
				.get("logout")
				.then()
				.statusCode(HttpStatus.SC_TEMPORARY_REDIRECT)
				.extract()
				.headers();

		List<String> setCookieHeaders = responseHeaders.getValues(HttpHeaders.SET_COOKIE);
		Assertions.assertTrue(setCookieHeaders.stream().flatMap(c -> HttpCookie.parse(c).stream()).allMatch(c -> c.getMaxAge() == 0));
	}
}
