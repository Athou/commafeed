package com.commafeed.integration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.common.net.HttpHeaders;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
class CompressionIT {

	@ParameterizedTest
	@ValueSource(strings = { "/rest/server/get", "/" })
	void servedWithCompression(String path) {
		RestAssured.given().when().get(path).then().statusCode(200).header(HttpHeaders.CONTENT_ENCODING, "gzip");
	}
}
