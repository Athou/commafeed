package com.commafeed.integration.servlet;

import jakarta.ws.rs.core.HttpHeaders;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.commafeed.TestConstants;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
class NextUnreadIT extends BaseIT {

	@BeforeEach
	void setup() {
		initialSetup(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
		RestAssured.authentication = RestAssured.preemptive().basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
	}

	@AfterEach
	void cleanup() {
		RestAssured.reset();
	}

	@Test
	void test() {
		subscribeAndWaitForEntries(getFeedUrl());

		RestAssured.given()
				.redirects()
				.follow(false)
				.get("next")
				.then()
				.statusCode(HttpStatus.SC_TEMPORARY_REDIRECT)
				.header(HttpHeaders.LOCATION, "https://hostname.local/commafeed/2");
	}

}
