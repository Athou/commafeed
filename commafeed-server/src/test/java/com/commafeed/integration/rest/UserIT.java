package com.commafeed.integration.rest;

import com.commafeed.TestConstants;
import com.commafeed.frontend.model.Settings;
import com.commafeed.frontend.model.request.PasswordResetConfirmationRequest;
import com.commafeed.frontend.model.request.PasswordResetRequest;
import com.commafeed.frontend.model.request.RegistrationRequest;
import com.commafeed.integration.BaseIT;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.vertx.ext.mail.MailMessage;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpCookie;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

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
        String session =
                login().stream().map(HttpCookie::toString).collect(Collectors.joining(";"));

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

        // sessions opened before the reset are no longer valid
        RestAssured.given()
                .auth()
                .none()
                .header(HttpHeaders.COOKIE, session)
                .get("rest/user/settings")
                .then()
                .statusCode(401);

        RestAssured.authentication =
                RestAssured.preemptive().basic(TestConstants.ADMIN_USERNAME, newPassword);
        RestAssured.given().get("rest/user/settings").then().statusCode(200);
    }

    @Test
    void resetPasswordEscapesUsernameInEmail() {
        String name = "<a href=//evil.example>CLICK</a>";
        String email = "victim@commafeed.com";

        RegistrationRequest registrationReq = new RegistrationRequest();
        registrationReq.setName(name);
        registrationReq.setPassword("MyPassword123!");
        registrationReq.setEmail(email);
        RestAssured.given()
                .body(registrationReq)
                .contentType(ContentType.JSON)
                .post("rest/user/register")
                .then()
                .statusCode(200);

        PasswordResetRequest req = new PasswordResetRequest();
        req.setEmail(email);
        RestAssured.given()
                .body(req)
                .contentType(ContentType.JSON)
                .post("rest/user/passwordReset")
                .then()
                .statusCode(200);

        List<MailMessage> mails = mailbox.getMailMessagesSentTo(email);
        Assertions.assertEquals(1, mails.size());

        String html = mails.getFirst().getHtml();
        Assertions.assertTrue(
                html.startsWith(
                        "You asked for password recovery for account '&lt;a href=//evil.example&gt;CLICK&lt;/a&gt;'"));

        Elements links = Jsoup.parse(html).select("a");
        Assertions.assertEquals(1, links.size());
        Assertions.assertTrue(
                links.getFirst().attr("href").startsWith("https://commafeed.example.com"));
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
}
