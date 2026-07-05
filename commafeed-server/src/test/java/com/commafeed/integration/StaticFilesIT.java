package com.commafeed.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
class StaticFilesIT {

    @ParameterizedTest
    @ValueSource(strings = {"/", "/openapi"})
    void servedWithoutCache(String path) {
        RestAssured.given()
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .header("Cache-Control", "no-cache");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/favicon.ico"})
    void servedWithCache(String path) {
        RestAssured.given()
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .header("Cache-Control", "public, immutable, max-age=31536000");
    }
}
