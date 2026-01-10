package com.commafeed.integration.rest;

import java.util.List;

import jakarta.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.commafeed.TestConstants;
import com.commafeed.frontend.model.Settings;
import com.commafeed.frontend.model.request.PasswordResetRequest;
import com.commafeed.integration.BaseIT;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.vertx.ext.mail.MailMessage;

@QuarkusTest
class UserIT extends BaseIT {

	@Inject
	MockMailbox mailbox;

	@BeforeEach
	void setup() {
		initialSetup(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
		RestAssured.authentication = RestAssured.preemptive().basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);

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
		RestAssured.given().body(req).contentType(ContentType.JSON).post("rest/user/passwordReset").then().statusCode(200);

		List<MailMessage> mails = mailbox.getMailMessagesSentTo("admin@commafeed.com");
		Assertions.assertEquals(1, mails.size());

		MailMessage message = mails.getFirst();
		Assertions.assertEquals("CommaFeed - Password recovery", message.getSubject());
		Assertions.assertTrue(message.getHtml().startsWith("You asked for password recovery for account 'admin'"));
		Assertions.assertEquals("admin@commafeed.com", message.getTo().getFirst());

		Element a = Jsoup.parse(message.getHtml()).select("a").getFirst();
		String link = a.attr("href");
		String newPasswordResponse = RestAssured.given().urlEncodingEnabled(false).get(link).then().statusCode(200).extract().asString();
		Assertions.assertTrue(newPasswordResponse.contains("Your new password is:"));
	}

	@Test
	void saveSettings() {
		Settings settings = RestAssured.given().get("rest/user/settings").then().extract().as(Settings.class);
		settings.setLanguage("test");
		RestAssured.given().body(settings).contentType(ContentType.JSON).post("rest/user/settings").then().statusCode(200);

		Settings updatedSettings = RestAssured.given().get("rest/user/settings").then().extract().as(Settings.class);
		Assertions.assertEquals("test", updatedSettings.getLanguage());
	}
}
