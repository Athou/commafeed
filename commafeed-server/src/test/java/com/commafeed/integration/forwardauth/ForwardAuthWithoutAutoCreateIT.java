package com.commafeed.integration.forwardauth;

import com.commafeed.TestConstants;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

@QuarkusTest
@TestProfile(ForwardAuthWithoutAutoCreateIT.ForwardAuthWithoutAutoCreateProfile.class)
class ForwardAuthWithoutAutoCreateIT extends BaseIT {

    @BeforeEach
    void setup() {
        initialSetup(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
    }

    @Test
    void existingUser() {
        RestAssured.given()
                .header("Remote-User", TestConstants.ADMIN_USERNAME)
                .get("rest/user/profile")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    void unknownUser() {
        RestAssured.given()
                .header("Remote-User", "sso-user")
                .get("rest/user/profile")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    public static class ForwardAuthWithoutAutoCreateProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "commafeed.forward-auth.enabled", "true",
                    "commafeed.forward-auth.trusted-proxies", "127.0.0.1,::1",
                    "commafeed.forward-auth.auto-create-users", "false");
        }
    }
}
