package com.commafeed.integration.servlet;

import org.apache.hc.core5.http.HttpStatus;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.commafeed.frontend.model.Settings;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
class CustomCodeIT extends BaseIT {

	@BeforeEach
	void setup() {
		RestAssured.authentication = RestAssured.preemptive().basic("admin", "admin");
	}

	@AfterEach
	void cleanup() {
		RestAssured.reset();
	}

	@Test
	void test() {
		// get settings
		Settings settings = RestAssured.given().get("rest/user/settings").then().statusCode(200).extract().as(Settings.class);

		// update settings
		settings.setCustomJs("custom-js");
		settings.setCustomCss("custom-css");
		RestAssured.given().body(settings).contentType(ContentType.JSON).post("rest/user/settings").then().statusCode(HttpStatus.SC_OK);

		// check custom code servlets
		RestAssured.given().get("custom_js.js").then().statusCode(HttpStatus.SC_OK).body(CoreMatchers.is("custom-js"));
		RestAssured.given().get("custom_css.css").then().statusCode(HttpStatus.SC_OK).body(CoreMatchers.is("custom-css"));
	}
}
