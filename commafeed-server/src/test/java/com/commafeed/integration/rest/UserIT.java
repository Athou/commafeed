package com.commafeed.integration.rest;

import com.commafeed.TestConstants;
import com.commafeed.frontend.model.Settings;
import com.commafeed.frontend.model.request.PasswordResetConfirmationRequest;
import com.commafeed.frontend.model.request.PasswordResetRequest;
import com.commafeed.integration.BaseIT;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.vertx.ext.mail.MailMessage;

import jakarta.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@QuarkusTest
class UserIT extends BaseIT {

    @Inject MockMailbox mailbox;

    @BeforeEach
    void setup() {
        initialSetup(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
        RestAssured.authentication =
                RestAssured.preemptive()
                        .basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);

        mailbox.clear();
    }

    @AfterEach
    void cleanup() {
        RestAssured.reset();
    }

    @Test
    void resetPassword() {
        PasswordResetRequest req = new PasswordResetRequest();
        req.setEmail("admin@commafeed.com");
        RestAssured.given()
                .body(req)
                .contentType(ContentType.JSON)
                .header("Host", "malicious.url.com")
                .post("rest/user/passwordReset")
                .then()
                .statusCode(200);

        List<MailMessage> mails = mailbox.getMailMessagesSentTo("admin@commafeed.com");
        Assertions.assertEquals(1, mails.size());

        MailMessage message = mails.getFirst();
        Assertions.assertEquals("CommaFeed - Password recovery", message.getSubject());
        Assertions.assertTrue(
                message.getHtml()
                        .startsWith("You asked for password recovery for account 'admin'"));
        Assertions.assertTrue(message.getHtml().contains("https://commafeed.example.com"));
        Assertions.assertEquals("admin@commafeed.com", message.getTo().getFirst());

        Element a = Jsoup.parse(message.getHtml()).select("a").getFirst();
        String link = a.attr("href");

        String email = null;
        String token = null;
        String queryString = link.substring(link.indexOf('?') + 1);
        for (String param : queryString.split("&")) {
            String[] keyValue = param.split("=");
            if ("email".equals(keyValue[0])) {
                email = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
            } else if ("token".equals(keyValue[0])) {
                token = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
            }
        }

        Assertions.assertNotNull(email);
        Assertions.assertNotNull(token);
        Assertions.assertTrue(link.contains("#/passwordReset?"));

        String newPassword = "MyNewPassword123!";
        PasswordResetConfirmationRequest confirmReq = new PasswordResetConfirmationRequest();
        confirmReq.setEmail(email);
        confirmReq.setToken(token);
        confirmReq.setPassword(newPassword);
        RestAssured.given()
                .body(confirmReq)
                .contentType(ContentType.JSON)
                .post("rest/user/passwordResetCallback")
                .then()
                .statusCode(200);

        RestAssured.authentication =
                RestAssured.preemptive().basic(TestConstants.ADMIN_USERNAME, newPassword);
        RestAssured.given().get("rest/user/settings").then().statusCode(200);
    }

    @Test
    void saveSettings() {
        Settings settings =
                RestAssured.given().get("rest/user/settings").then().extract().as(Settings.class);
        settings.setLanguage("test");
        RestAssured.given()
                .body(settings)
                .contentType(ContentType.JSON)
                .post("rest/user/settings")
                .then()
                .statusCode(200);

        Settings updatedSettings =
                RestAssured.given().get("rest/user/settings").then().extract().as(Settings.class);
        Assertions.assertEquals("test", updatedSettings.getLanguage());
    }

    @Test
    void saveCustomSharingDestinations() {
        Settings settings =
                RestAssured.given().get("rest/user/settings").then().extract().as(Settings.class);

        Settings.CustomSharingDestination destination = new Settings.CustomSharingDestination();
        destination.setName("Wallabag");
        destination.setUrlPattern("https://example.com/save?url=${url}");
        destination.setIcon("SiWallabag");
        settings.setCustomSharingDestinations(List.of(destination));

        RestAssured.given()
                .body(settings)
                .contentType(ContentType.JSON)
                .post("rest/user/settings")
                .then()
                .statusCode(200);

        Settings updatedSettings =
                RestAssured.given().get("rest/user/settings").then().extract().as(Settings.class);
        Assertions.assertEquals(1, updatedSettings.getCustomSharingDestinations().size());
        Settings.CustomSharingDestination updatedDestination =
                updatedSettings.getCustomSharingDestinations().getFirst();
        Assertions.assertEquals("Wallabag", updatedDestination.getName());
        Assertions.assertEquals(
                "https://example.com/save?url=${url}", updatedDestination.getUrlPattern());
        Assertions.assertEquals("SiWallabag", updatedDestination.getIcon());
    }

    @Test
    void rejectsCustomSharingDestinationWithInvalidScheme() {
        Settings settings =
                RestAssured.given().get("rest/user/settings").then().extract().as(Settings.class);

        Settings.CustomSharingDestination destination = new Settings.CustomSharingDestination();
        destination.setName("Malicious");
        destination.setUrlPattern("javascript:alert(1)");
        destination.setIcon("SiWallabag");
        settings.setCustomSharingDestinations(List.of(destination));

        RestAssured.given()
                .body(settings)
                .contentType(ContentType.JSON)
                .post("rest/user/settings")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectsCustomSharingDestinationWithBlankName() {
        Settings settings =
                RestAssured.given().get("rest/user/settings").then().extract().as(Settings.class);

        Settings.CustomSharingDestination destination = new Settings.CustomSharingDestination();
        destination.setName("");
        destination.setUrlPattern("https://example.com/save?url=${url}");
        destination.setIcon("SiWallabag");
        settings.setCustomSharingDestinations(List.of(destination));

        RestAssured.given()
                .body(settings)
                .contentType(ContentType.JSON)
                .post("rest/user/settings")
                .then()
                .statusCode(400);
    }

    @Test
    void acceptsCustomSharingDestinationWithUppercaseScheme() {
        Settings settings =
                RestAssured.given().get("rest/user/settings").then().extract().as(Settings.class);

        Settings.CustomSharingDestination destination = new Settings.CustomSharingDestination();
        destination.setName("Uppercase");
        destination.setUrlPattern("HTTPS://example.com/save?url=${url}");
        destination.setIcon("SiWallabag");
        settings.setCustomSharingDestinations(List.of(destination));

        RestAssured.given()
                .body(settings)
                .contentType(ContentType.JSON)
                .post("rest/user/settings")
                .then()
                .statusCode(200);
    }

    @Test
    void rejectsCustomSharingDestinationWithTooLongName() {
        Settings settings =
                RestAssured.given().get("rest/user/settings").then().extract().as(Settings.class);

        Settings.CustomSharingDestination destination = new Settings.CustomSharingDestination();
        destination.setName("a".repeat(129));
        destination.setUrlPattern("https://example.com/save?url=${url}");
        destination.setIcon("SiWallabag");
        settings.setCustomSharingDestinations(List.of(destination));

        RestAssured.given()
                .body(settings)
                .contentType(ContentType.JSON)
                .post("rest/user/settings")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectsCustomSharingDestinationWithTooLongUrlPattern() {
        Settings settings =
                RestAssured.given().get("rest/user/settings").then().extract().as(Settings.class);

        Settings.CustomSharingDestination destination = new Settings.CustomSharingDestination();
        destination.setName("Long");
        destination.setUrlPattern("https://example.com/?q=" + "a".repeat(1024));
        destination.setIcon("SiWallabag");
        settings.setCustomSharingDestinations(List.of(destination));

        RestAssured.given()
                .body(settings)
                .contentType(ContentType.JSON)
                .post("rest/user/settings")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectsCustomSharingDestinationsWithDuplicateNames() {
        Settings settings =
                RestAssured.given().get("rest/user/settings").then().extract().as(Settings.class);

        Settings.CustomSharingDestination first = new Settings.CustomSharingDestination();
        first.setName("Readeck");
        first.setUrlPattern("https://example.com/save?url=${url}");
        first.setIcon("SiWallabag");

        Settings.CustomSharingDestination second = new Settings.CustomSharingDestination();
        // same name in a different case: they would be indistinguishable in the share menu
        second.setName("readeck");
        second.setUrlPattern("https://other.example.com/save?url=${url}");
        second.setIcon("SiPinboard");

        settings.setCustomSharingDestinations(List.of(first, second));

        RestAssured.given()
                .body(settings)
                .contentType(ContentType.JSON)
                .post("rest/user/settings")
                .then()
                .statusCode(400);
    }
}
