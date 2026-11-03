package com.commafeed.integration;

import com.commafeed.ExceptionMappers.CommaFeedApplicationError;
import com.commafeed.ExceptionMappers.UnauthorizedResponse;
import com.commafeed.TestConstants;
import com.commafeed.frontend.exception.CommaFeedExceptionType;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.AdminSaveUserRequest;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.frontend.model.request.ProfileModificationRequest;
import com.commafeed.frontend.model.request.SubscribeRequest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import jakarta.ws.rs.core.HttpHeaders;

import org.apache.hc.core5.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpCookie;
import java.util.List;
import java.util.stream.Collectors;

@QuarkusTest
class SecurityIT extends BaseIT {

    private static final String USER_NAME = "session-user";
    private static final String USER_PASSWORD = "MyPassword123!";
    private static final String NEW_USER_PASSWORD = "MyNewPassword123!";

    @BeforeEach
    void setup() {
        initialSetup(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
    }

    @Test
    void notLoggedIn() {
        UnauthorizedResponse info =
                RestAssured.given()
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
    void formLoginWrongPassword() {
        CommaFeedApplicationError error =
                RestAssured.given()
                        .auth()
                        .none()
                        .formParams("j_username", TestConstants.ADMIN_USERNAME, "j_password", "wrong-password")
                        .post("j_security_check")
                        .then()
                        .statusCode(HttpStatus.SC_UNAUTHORIZED)
                        .extract()
                        .as(CommaFeedApplicationError.class);

        Assertions.assertEquals(CommaFeedExceptionType.WRONG_USERNAME_OR_PASSWORD, error.type());
        Assertions.assertEquals("wrong username or password", error.message());
    }

    @Test
    void basicAuthLogin() {
        RestAssured.given()
                .auth()
                .preemptive()
                .basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD)
                .get("rest/user/profile")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    void wrongPassword() {
        RestAssured.given()
                .auth()
                .preemptive()
                .basic(TestConstants.ADMIN_USERNAME, "wrong-password")
                .get("rest/user/profile")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void missingRole() {
        RestAssured.given()
                .auth()
                .preemptive()
                .basic("demo", "demo")
                .get("rest/admin/metrics")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void apiKey() {
        // create api key
        ProfileModificationRequest req = new ProfileModificationRequest();
        req.setCurrentPassword(TestConstants.ADMIN_PASSWORD);
        req.setNewApiKey(true);
        RestAssured.given()
                .auth()
                .preemptive()
                .basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD)
                .body(req)
                .contentType(ContentType.JSON)
                .post("rest/user/profile")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // fetch api key
        String apiKey =
                RestAssured.given()
                        .auth()
                        .preemptive()
                        .basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD)
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
        long subscriptionId =
                RestAssured.given()
                        .auth()
                        .preemptive()
                        .basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD)
                        .body(subscribeRequest)
                        .contentType(ContentType.JSON)
                        .post("rest/feed/subscribe")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .as(Long.class);

        // get entries with api key
        Entries entries =
                RestAssured.given()
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

    @Test
    void selfPasswordChangeInvalidatesSessions() {
        createUser(false);
        String sessionA = session(USER_NAME, USER_PASSWORD);
        String sessionB = session(USER_NAME, USER_PASSWORD);
        String adminSession = session(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);

        ProfileModificationRequest req = new ProfileModificationRequest();
        req.setCurrentPassword(USER_PASSWORD);
        req.setNewPassword(NEW_USER_PASSWORD);
        RestAssured.given()
                .header(HttpHeaders.COOKIE, sessionA)
                .body(req)
                .contentType(ContentType.JSON)
                .post("rest/user/profile")
                .then()
                .statusCode(HttpStatus.SC_OK);

        assertSessionStatus(sessionA, HttpStatus.SC_UNAUTHORIZED);
        assertSessionStatus(sessionB, HttpStatus.SC_UNAUTHORIZED);
        assertSessionStatus(adminSession, HttpStatus.SC_OK);
        assertSessionStatus(session(USER_NAME, NEW_USER_PASSWORD), HttpStatus.SC_OK);
    }

    @Test
    void profileChangeWithoutNewPasswordKeepsSessions() {
        createUser(false);
        String session = session(USER_NAME, USER_PASSWORD);

        ProfileModificationRequest req = new ProfileModificationRequest();
        req.setCurrentPassword(USER_PASSWORD);
        req.setEmail("session-user@commafeed.com");
        RestAssured.given()
                .header(HttpHeaders.COOKIE, session)
                .body(req)
                .contentType(ContentType.JSON)
                .post("rest/user/profile")
                .then()
                .statusCode(HttpStatus.SC_OK);

        assertSessionStatus(session, HttpStatus.SC_OK);
    }

    @Test
    void adminPasswordChangeInvalidatesSessionsAndRotatesApiKey() {
        Long id = createUser(false);
        String session = session(USER_NAME, USER_PASSWORD);

        ProfileModificationRequest apiKeyReq = new ProfileModificationRequest();
        apiKeyReq.setCurrentPassword(USER_PASSWORD);
        apiKeyReq.setNewApiKey(true);
        RestAssured.given()
                .header(HttpHeaders.COOKIE, session)
                .body(apiKeyReq)
                .contentType(ContentType.JSON)
                .post("rest/user/profile")
                .then()
                .statusCode(HttpStatus.SC_OK);
        String apiKey = profile(session).getApiKey();
        Assertions.assertNotNull(apiKey);

        AdminSaveUserRequest req = saveUserRequest(false);
        req.setId(id);
        req.setPassword(NEW_USER_PASSWORD);
        saveUser(req);

        assertSessionStatus(session, HttpStatus.SC_UNAUTHORIZED);
        String newSession = session(USER_NAME, NEW_USER_PASSWORD);
        assertSessionStatus(newSession, HttpStatus.SC_OK);
        Assertions.assertNotEquals(apiKey, profile(newSession).getApiKey());
    }

    @Test
    void adminPasswordChangeDoesNotCreateApiKey() {
        Long id = createUser(false);

        AdminSaveUserRequest req = saveUserRequest(false);
        req.setId(id);
        req.setPassword(NEW_USER_PASSWORD);
        saveUser(req);

        Assertions.assertNull(profile(session(USER_NAME, NEW_USER_PASSWORD)).getApiKey());
    }

    @Test
    void disablingUserInvalidatesSessions() {
        Long id = createUser(false);
        String session = session(USER_NAME, USER_PASSWORD);
        assertSessionStatus(session, HttpStatus.SC_OK);

        AdminSaveUserRequest req = saveUserRequest(false);
        req.setId(id);
        req.setEnabled(false);
        saveUser(req);

        assertSessionStatus(session, HttpStatus.SC_UNAUTHORIZED);

        // sessions opened before the user was disabled stay invalid after enabling it again
        req.setEnabled(true);
        saveUser(req);

        assertSessionStatus(session, HttpStatus.SC_UNAUTHORIZED);
        assertSessionStatus(session(USER_NAME, USER_PASSWORD), HttpStatus.SC_OK);
    }

    @Test
    void savingUserWithoutChangesKeepsSessions() {
        Long id = createUser(false);
        String session = session(USER_NAME, USER_PASSWORD);

        AdminSaveUserRequest req = saveUserRequest(false);
        req.setId(id);
        saveUser(req);

        assertSessionStatus(session, HttpStatus.SC_OK);
    }

    @Test
    void disablingAdminRevokesAdminAccess() {
        Long id = createUser(true);
        String session = session(USER_NAME, USER_PASSWORD);
        RestAssured.given()
                .header(HttpHeaders.COOKIE, session)
                .get("rest/admin/metrics")
                .then()
                .statusCode(HttpStatus.SC_OK);

        AdminSaveUserRequest req = saveUserRequest(true);
        req.setId(id);
        req.setEnabled(false);
        saveUser(req);

        RestAssured.given()
                .header(HttpHeaders.COOKIE, session)
                .get("rest/admin/metrics")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void invalidatedSessionRemovesCookie() {
        Long id = createUser(false);
        String session = session(USER_NAME, USER_PASSWORD);

        AdminSaveUserRequest req = saveUserRequest(false);
        req.setId(id);
        req.setEnabled(false);
        saveUser(req);

        // api calls receive an error
        String apiSetCookie =
                RestAssured.given()
                        .header(HttpHeaders.COOKIE, session)
                        .get("rest/user/profile")
                        .then()
                        .statusCode(HttpStatus.SC_UNAUTHORIZED)
                        .extract()
                        .header(HttpHeaders.SET_COOKIE);
        Assertions.assertTrue(apiSetCookie.contains("Max-Age=0"));

        // browsers are redirected to the page they asked for, this time without the cookie
        String browserSetCookie =
                RestAssured.given()
                        .redirects()
                        .follow(false)
                        .header(HttpHeaders.COOKIE, session)
                        .header(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,*/*;q=0.8")
                        .get("next?category=all")
                        .then()
                        .statusCode(HttpStatus.SC_TEMPORARY_REDIRECT)
                        .header(HttpHeaders.LOCATION, Matchers.endsWith("/next?category=all"))
                        .extract()
                        .header(HttpHeaders.SET_COOKIE);
        Assertions.assertTrue(browserSetCookie.contains("Max-Age=0"));
    }

    private Long createUser(boolean admin) {
        AdminSaveUserRequest req = saveUserRequest(admin);
        req.setPassword(USER_PASSWORD);
        return saveUser(req);
    }

    private AdminSaveUserRequest saveUserRequest(boolean admin) {
        AdminSaveUserRequest req = new AdminSaveUserRequest();
        req.setName(USER_NAME);
        req.setEnabled(true);
        req.setAdmin(admin);
        return req;
    }

    private Long saveUser(AdminSaveUserRequest req) {
        return RestAssured.given()
                .auth()
                .preemptive()
                .basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD)
                .body(req)
                .contentType(ContentType.JSON)
                .post("rest/admin/user/save")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(Long.class);
    }

    private String session(String userName, String password) {
        return login(userName, password).stream().map(HttpCookie::toString).collect(Collectors.joining(";"));
    }

    private UserModel profile(String session) {
        return RestAssured.given()
                .header(HttpHeaders.COOKIE, session)
                .get("rest/user/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(UserModel.class);
    }

    private void assertSessionStatus(String session, int status) {
        RestAssured.given().header(HttpHeaders.COOKIE, session).get("rest/user/profile").then().statusCode(status);
    }
}
