package com.commafeed.integration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
class StaticFilesIT {

	@ParameterizedTest
	@ValueSource(strings = { "/", "/openapi.json", "/openapi.yaml" })
	void servedWithoutCache(String path) {
		RestAssured.given().when().get(path).then().statusCode(200).header("Cache-Control", "no-cache");
	}

	@ParameterizedTest
	@ValueSource(strings = { "/favicon.ico" })
	void servedWithCache(String path) {
		RestAssured.given().when().get(path).then().statusCode(200).header("Cache-Control", "public, immutable, max-age=2592000");
	}
}
