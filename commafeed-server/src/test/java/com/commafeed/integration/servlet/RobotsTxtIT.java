package com.commafeed.integration.servlet;

import com.commafeed.integration.BaseIT;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

@QuarkusTest
class RobotsTxtIT extends BaseIT {
    @Test
    void test() {
        RestAssured.given()
                .get("robots.txt")
                .then()
                .statusCode(200)
                .body(CoreMatchers.is("User-agent: *\nDisallow: /"));
    }
}
