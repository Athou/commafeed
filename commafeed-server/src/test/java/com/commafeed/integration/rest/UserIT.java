package com.commafeed.integration.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.commafeed.frontend.model.request.PasswordResetRequest;
import com.commafeed.integration.BaseIT;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;

import jakarta.mail.internet.MimeMessage;
import jakarta.ws.rs.client.Entity;

class UserIT extends BaseIT {

	@Nested
	class PasswordReset {

		@RegisterExtension
		static final GreenMailExtension GREEN_MAIL = new GreenMailExtension(ServerSetupTest.SMTP);

		@BeforeEach
		void init() {
			GREEN_MAIL.setUser("noreply@commafeed.com", "user", "pass");
		}

		@Test
		void resetPassword() throws Exception {
			PasswordResetRequest req = new PasswordResetRequest();
			req.setEmail("admin@commafeed.com");

			getClient().target(getApiBaseUrl() + "user/passwordReset").request().post(Entity.json(req), Void.TYPE);

			MimeMessage message = GREEN_MAIL.getReceivedMessages()[0];
			Assertions.assertEquals("CommaFeed - Password recovery", message.getSubject());
			Assertions.assertTrue(message.getContent().toString().startsWith("You asked for password recovery for account 'admin'"));
			Assertions.assertEquals("CommaFeed <noreply@commafeed.com>", message.getFrom()[0].toString());
			Assertions.assertEquals("admin@commafeed.com", message.getAllRecipients()[0].toString());
		}
	}
}
