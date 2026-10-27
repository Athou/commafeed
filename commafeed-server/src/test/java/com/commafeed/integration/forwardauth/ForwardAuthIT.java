package com.commafeed.integration.forwardauth;

import com.commafeed.TestConstants;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.AdminSaveUserRequest;
import com.commafeed.frontend.model.request.ProfileModificationRequest;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import jakarta.ws.rs.core.HttpHeaders;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@QuarkusTest
@TestProfile(ForwardAuthIT.ForwardAuthProfile.class)
class ForwardAuthIT extends BaseIT {

    @BeforeEach
    void setup() {
        initialSetup(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
    }

    @Test
    void createsUser() {
        UserModel profile =
                getProfile(asUser("sso-user").header("Remote-Email", "sso@example.com"));
        Assertions.assertEquals("sso-user", profile.getName());
        Assertions.assertEquals("sso@example.com", profile.getEmail());
        Assertions.assertTrue(profile.isForwardAuthenticated());
        Assertions.assertFalse(profile.isAdmin());

        // second request uses the same user
        Assertions.assertEquals(profile.getId(), getProfile(asUser("sso-user")).getId());
    }

    @Test
    void existingUser() {
        UserModel profile = getProfile(asUser(TestConstants.ADMIN_USERNAME));
        Assertions.assertEquals(TestConstants.ADMIN_USERNAME, profile.getName());
        Assertions.assertTrue(profile.isForwardAuthenticated());
        Assertions.assertTrue(profile.isAdmin());
    }

    @Test
    void emailAlreadyTaken() {
        UserModel profile =
                getProfile(
                        asUser("sso-user")
                                .header(
                                        "Remote-Email",
                                        TestConstants.ADMIN_USERNAME + "@commafeed.com"));
        Assertions.assertEquals("sso-user", profile.getName());
        Assertions.assertNull(profile.getEmail());
    }

    @Test
    void adminGroup() {
        asUser("sso-admin")
                .header("Remote-Groups", "users,admins")
                .get("rest/admin/metrics")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // admin role is revoked when the user is no longer in the admin group
        asUser("sso-admin")
                .header("Remote-Groups", "users")
                .get("rest/admin/metrics")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void lastAdminIsKept() {
        asUser(TestConstants.ADMIN_USERNAME)
                .header("Remote-Groups", "users")
                .get("rest/admin/metrics")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    void concurrentAdminRevocationKeepsLastAdmin() throws Exception {
        asUser("sso-admin-1").header("Remote-Groups", "admins").get("rest/user/profile");
        asUser("sso-admin-2").header("Remote-Groups", "admins").get("rest/user/profile");
        // revoke the admin role of the initial admin so that only the two sso admins are left
        Assertions.assertFalse(
                getProfile(asUser(TestConstants.ADMIN_USERNAME).header("Remote-Groups", "users"))
                        .isAdmin());

        // both sso admins are removed from the admin group at the same time
        List<Callable<Integer>> requests =
                IntStream.range(0, 20)
                        .mapToObj(i -> "sso-admin-" + (i % 2 + 1))
                        .<Callable<Integer>>map(
                                username ->
                                        () ->
                                                asUser(username)
                                                        .header("Remote-Groups", "users")
                                                        .get("rest/user/profile")
                                                        .statusCode())
                        .toList();
        try (ExecutorService executor = Executors.newFixedThreadPool(requests.size())) {
            for (Future<Integer> future : executor.invokeAll(requests)) {
                Assertions.assertEquals(HttpStatus.SC_OK, future.get());
            }
        }

        // the last admin is never revoked, so one of them must still be admin
        boolean admin1 =
                getProfile(asUser("sso-admin-1").header("Remote-Groups", "users")).isAdmin();
        boolean admin2 =
                getProfile(asUser("sso-admin-2").header("Remote-Groups", "users")).isAdmin();
        Assertions.assertTrue(admin1 ^ admin2);
    }

    @Test
    void disabledUser() {
        UserModel profile = getProfile(asUser("sso-user"));

        AdminSaveUserRequest req = new AdminSaveUserRequest();
        req.setId(profile.getId());
        req.setName(profile.getName());
        req.setEnabled(false);
        asAdmin()
                .body(req)
                .contentType(ContentType.JSON)
                .post("rest/admin/user/save")
                .then()
                .statusCode(HttpStatus.SC_OK);

        asUser("sso-user").get("rest/user/profile").then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void headerTakesPrecedenceOverSessionCookie() {
        List<HttpCookie> cookies = login();
        UserModel profile =
                getProfile(
                        asUser("sso-user")
                                .header(
                                        HttpHeaders.COOKIE,
                                        cookies.stream()
                                                .map(HttpCookie::toString)
                                                .collect(Collectors.joining(";"))));
        Assertions.assertEquals("sso-user", profile.getName());
    }

    @Test
    void passwordAuthenticationStillWorks() {
        UserModel profile =
                getProfile(
                        RestAssured.given()
                                .auth()
                                .preemptive()
                                .basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD));
        Assertions.assertFalse(profile.isForwardAuthenticated());
    }

    @Test
    void profileModificationWithoutPassword() {
        String apiKey = getProfile(asUser("sso-user")).getApiKey();

        ProfileModificationRequest req = new ProfileModificationRequest();
        req.setNewApiKey(true);
        asUser("sso-user")
                .body(req)
                .contentType(ContentType.JSON)
                .post("rest/user/profile")
                .then()
                .statusCode(HttpStatus.SC_OK);

        Assertions.assertNotEquals(apiKey, getProfile(asUser("sso-user")).getApiKey());
    }

    @Test
    void profileModificationStillRequiresPasswordWithoutForwardAuth() {
        ProfileModificationRequest req = new ProfileModificationRequest();
        req.setNewApiKey(true);
        RestAssured.given()
                .auth()
                .preemptive()
                .basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD)
                .body(req)
                .contentType(ContentType.JSON)
                .post("rest/user/profile")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void logoutRedirectsToSingleSignOnProvider() {
        String location =
                asUser("sso-user")
                        .redirects()
                        .follow(false)
                        .get("logout")
                        .then()
                        .statusCode(HttpStatus.SC_TEMPORARY_REDIRECT)
                        .extract()
                        .header(HttpHeaders.LOCATION);
        Assertions.assertEquals("https://sso.example.com/logout", location);
    }

    @Test
    void logoutWithPasswordStaysOnCommaFeed() {
        String location =
                asAdmin()
                        .redirects()
                        .follow(false)
                        .get("logout")
                        .then()
                        .statusCode(HttpStatus.SC_TEMPORARY_REDIRECT)
                        .extract()
                        .header(HttpHeaders.LOCATION);
        Assertions.assertEquals(getBaseUrl(), location);
    }

    @Test
    void noHeader() {
        RestAssured.given().get("rest/user/profile").then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    private RequestSpecification asUser(String username) {
        return RestAssured.given().header("Remote-User", username);
    }

    private RequestSpecification asAdmin() {
        return RestAssured.given()
                .auth()
                .preemptive()
                .basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
    }

    private UserModel getProfile(RequestSpecification request) {
        return request.get("rest/user/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(UserModel.class);
    }

    public static class ForwardAuthProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "commafeed.forward-auth.enabled", "true",
                    "commafeed.forward-auth.trusted-proxies", "127.0.0.1,::1",
                    "commafeed.forward-auth.admin-group", "admins",
                    "commafeed.forward-auth.logout-url", "https://sso.example.com/logout");
        }
    }
}
