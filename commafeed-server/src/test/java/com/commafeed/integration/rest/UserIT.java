package com.commafeed.integration.rest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.commafeed.frontend.model.request.PasswordResetRequest;
import com.commafeed.integration.BaseIT;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.mail.internet.MimeMessage;
import jakarta.ws.rs.client.Entity;

@QuarkusTest
class UserIT extends BaseIT {

	@Nested
	class PasswordReset {

		private GreenMail greenMail;

		@BeforeEach
		void setup() {
			this.greenMail = new GreenMail(ServerSetupTest.SMTP);
			this.greenMail.start();
			this.greenMail.setUser("noreply@commafeed.com", "user", "pass");
		}

		@AfterEach
		void cleanup() {
			this.greenMail.stop();
		}

		@Test
		void resetPassword() throws Exception {
			PasswordResetRequest req = new PasswordResetRequest();
			req.setEmail("admin@commafeed.com");

			getClient().target(getApiBaseUrl() + "user/passwordReset").request().post(Entity.json(req), Void.TYPE);

			MimeMessage message = greenMail.getReceivedMessages()[0];
			Assertions.assertEquals("CommaFeed - Password recovery", message.getSubject());
			Assertions.assertTrue(message.getContent().toString().startsWith("You asked for password recovery for account 'admin'"));
			Assertions.assertEquals("CommaFeed <noreply@commafeed.com>", message.getFrom()[0].toString());
			Assertions.assertEquals("admin@commafeed.com", message.getAllRecipients()[0].toString());
		}
	}
}
