package com.commafeed.integration.rest;

import com.commafeed.TestConstants;
import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.ProfileModificationRequest;
import com.commafeed.frontend.resource.googlereader.GoogleReaderModel.ItemRefs;
import com.commafeed.frontend.resource.googlereader.GoogleReaderModel.StreamContents;
import com.commafeed.frontend.resource.googlereader.GoogleReaderModel.SubscriptionList;
import com.commafeed.frontend.resource.googlereader.GoogleReaderModel.TagList;
import com.commafeed.frontend.resource.googlereader.GoogleReaderModel.UnreadCounts;
import com.commafeed.frontend.resource.googlereader.GoogleReaderModel.UserInfo;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import lombok.Getter;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@QuarkusTest
class GoogleReaderIT extends BaseIT {

    private static final String BASE = "rest/googlereader";

    @Getter private String apiKey;
    private GoogleReaderClient client;

    @BeforeEach
    void setup() {
        initialSetup(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
        RestAssured.authentication =
                RestAssured.preemptive()
                        .basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);

        // create api key
        ProfileModificationRequest req = new ProfileModificationRequest();
        req.setCurrentPassword(TestConstants.ADMIN_PASSWORD);
        req.setNewApiKey(true);
        RestAssured.given()
                .body(req)
                .contentType(ContentType.JSON)
                .post("rest/user/profile")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // retrieve api key
        UserModel user =
                RestAssured.given()
                        .get("rest/user/profile")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .as(UserModel.class);
        this.apiKey = user.getApiKey();
        this.client = new GoogleReaderClient(clientLogin(TestConstants.ADMIN_USERNAME, apiKey));
    }

    @AfterEach
    void cleanup() {
        RestAssured.reset();
    }

    private String clientLogin(String email, String password) {
        String body =
                RestAssured.given()
                        .auth()
                        .none()
                        .formParam("Email", email)
                        .formParam("Passwd", password)
                        .post(BASE + "/accounts/ClientLogin")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .asString();

        Map<String, String> values =
                Arrays.stream(body.split("\n"))
                        .map(line -> line.split("=", 2))
                        .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
        return values.get("Auth");
    }

    @Test
    void invalidClientLogin() {
        RestAssured.given()
                .auth()
                .none()
                .formParam("Email", TestConstants.ADMIN_USERNAME)
                .formParam("Passwd", "invalid-key")
                .post(BASE + "/accounts/ClientLogin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void validClientLogin() {
        String token = clientLogin(TestConstants.ADMIN_USERNAME, apiKey);
        Assertions.assertNotNull(token);
        Assertions.assertTrue(token.endsWith("/" + apiKey));
    }

    @Test
    void invalidAuthToken() {
        Response response =
                RestAssured.given()
                        .auth()
                        .none()
                        .header("Authorization", "GoogleLogin auth=invalid/invalid-key")
                        .get(BASE + "/reader/api/0/user-info");
        Assertions.assertEquals(HttpStatus.SC_UNAUTHORIZED, response.statusCode());
    }

    @Test
    void authTokenOnlyAppliesToGoogleReaderEndpoints() {
        String token = clientLogin(TestConstants.ADMIN_USERNAME, apiKey);
        Response response =
                RestAssured.given()
                        .auth()
                        .none()
                        .header("Authorization", "GoogleLogin auth=" + token)
                        .get("rest/user/profile");
        Assertions.assertNotEquals(HttpStatus.SC_OK, response.statusCode());
    }

    @Test
    void userInfo() {
        UserInfo info = client.get(BASE + "/reader/api/0/user-info").as(UserInfo.class);
        Assertions.assertEquals(TestConstants.ADMIN_USERNAME, info.getUserName());
    }

    @Test
    void subscriptionList() {
        subscribe(getFeedUrl());
        SubscriptionList list =
                client.get(BASE + "/reader/api/0/subscription/list").as(SubscriptionList.class);
        Assertions.assertEquals(1, list.getSubscriptions().size());
        Assertions.assertEquals(
                "my title for this feed", list.getSubscriptions().getFirst().getTitle());
    }

    @Test
    void tagList() {
        createCategory("test-category");
        TagList tagList = client.get(BASE + "/reader/api/0/tag/list").as(TagList.class);
        Assertions.assertTrue(
                tagList.getTags().stream()
                        .anyMatch(t -> t.getId().equals("user/-/label/test-category")));
        Assertions.assertTrue(
                tagList.getTags().stream()
                        .anyMatch(t -> t.getId().equals("user/-/state/com.google/starred")));
    }

    @Test
    void unreadCount() {
        subscribeAndWaitForEntries(getFeedUrl());
        UnreadCounts counts =
                client.get(BASE + "/reader/api/0/unread-count").as(UnreadCounts.class);
        Assertions.assertTrue(
                counts.getUnreadCounts().stream()
                        .anyMatch(
                                c ->
                                        c.getId().equals("user/-/state/com.google/reading-list")
                                                && c.getCount() == 2));
    }

    @Test
    void streamContents() {
        subscribeAndWaitForEntries(getFeedUrl());
        StreamContents contents =
                client.get(BASE + "/reader/api/0/stream/contents/reading-list")
                        .as(StreamContents.class);
        Assertions.assertEquals(2, contents.getItems().size());
        Assertions.assertEquals("Item 2", contents.getItems().getFirst().getTitle());
        Assertions.assertTrue(
                contents.getItems()
                        .getFirst()
                        .getId()
                        .matches("tag:google\\.com,2005:reader/item/[0-9a-f]{16}"));
    }

    @Test
    void streamItemIds() {
        subscribeAndWaitForEntries(getFeedUrl());
        ItemRefs refs =
                client.get(
                                BASE
                                        + "/reader/api/0/stream/items/ids?s=user/-/state/com.google/reading-list")
                        .as(ItemRefs.class);
        Assertions.assertEquals(2, refs.getItemRefs().size());
    }

    @Test
    void editTagMarkRead() {
        Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
        Entry entry = getFeedEntries(subscriptionId).getEntries().getFirst();

        String token = client.get(BASE + "/reader/api/0/token").asString();
        client.postForm(
                BASE + "/reader/api/0/edit-tag",
                Map.of("i", entry.getId(), "a", "user/-/state/com.google/read", "T", token));

        Assertions.assertEquals(
                1,
                getFeedEntries(subscriptionId).getEntries().stream().filter(Entry::isRead).count());

        client.postForm(
                BASE + "/reader/api/0/edit-tag",
                Map.of("i", entry.getId(), "r", "user/-/state/com.google/read", "T", token));

        Assertions.assertEquals(
                0,
                getFeedEntries(subscriptionId).getEntries().stream().filter(Entry::isRead).count());
    }

    @Test
    void editTagMarkReadWithoutToken() {
        // some real-world Google Reader API clients never fetch the edit token and always submit
        // a blank one, this must still be accepted
        Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
        Entry entry = getFeedEntries(subscriptionId).getEntries().getFirst();

        client.postForm(
                BASE + "/reader/api/0/edit-tag",
                Map.of("i", entry.getId(), "a", "user/-/state/com.google/read"));

        Assertions.assertEquals(
                1,
                getFeedEntries(subscriptionId).getEntries().stream().filter(Entry::isRead).count());
    }

    @Test
    void editTagStar() {
        Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
        Entry entry = getFeedEntries(subscriptionId).getEntries().getFirst();

        String token = client.get(BASE + "/reader/api/0/token").asString();
        client.postForm(
                BASE + "/reader/api/0/edit-tag",
                Map.of("i", entry.getId(), "a", "user/-/state/com.google/starred", "T", token));

        Assertions.assertEquals(
                1,
                getFeedEntries(subscriptionId).getEntries().stream()
                        .filter(Entry::isStarred)
                        .count());
    }

    @Test
    void markAllAsRead() {
        Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());

        String token = client.get(BASE + "/reader/api/0/token").asString();
        client.postForm(
                BASE + "/reader/api/0/mark-all-as-read",
                Map.of("s", "user/-/state/com.google/reading-list", "T", token));

        Assertions.assertTrue(
                getFeedEntries(subscriptionId).getEntries().stream().allMatch(Entry::isRead));
    }

    @Test
    void subscribeAndUnsubscribe() {
        String token = client.get(BASE + "/reader/api/0/token").asString();

        client.postForm(
                BASE + "/reader/api/0/subscription/edit",
                Map.of(
                        "ac",
                        "subscribe",
                        "s",
                        "feed/" + getFeedUrl(),
                        "t",
                        "new subscription",
                        "T",
                        token));

        SubscriptionList list =
                client.get(BASE + "/reader/api/0/subscription/list").as(SubscriptionList.class);
        Assertions.assertEquals(1, list.getSubscriptions().size());
        String feedStreamId = list.getSubscriptions().getFirst().getId();

        client.postForm(
                BASE + "/reader/api/0/subscription/edit",
                Map.of("ac", "unsubscribe", "s", feedStreamId, "T", token));

        list = client.get(BASE + "/reader/api/0/subscription/list").as(SubscriptionList.class);
        Assertions.assertTrue(list.getSubscriptions().isEmpty());
    }

    private record GoogleReaderClient(String token) {

        private RequestSpecification given() {
            return RestAssured.given()
                    .auth()
                    .none()
                    .header("Authorization", "GoogleLogin auth=" + token);
        }

        Response get(String path) {
            return given().get(path).then().statusCode(HttpStatus.SC_OK).extract().response();
        }

        void postForm(String path, Map<String, ?> form) {
            RequestSpecification spec = given();
            form.forEach(spec::formParam);
            spec.post(path).then().statusCode(HttpStatus.SC_OK);
        }
    }
}
