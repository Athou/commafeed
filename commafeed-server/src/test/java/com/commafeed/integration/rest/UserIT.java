package com.commafeed.integration.rest;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.commafeed.frontend.model.request.PasswordResetRequest;
import com.commafeed.integration.BaseIT;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.ext.mail.MailMessage;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;

@QuarkusTest
class UserIT extends BaseIT {

	@Inject
	MockMailbox mailbox;

	@BeforeEach
	void setup() {
		mailbox.clear();
	}

	@Test
	void resetPassword() {
		PasswordResetRequest req = new PasswordResetRequest();
		req.setEmail("admin@commafeed.com");

		getClient().target(getApiBaseUrl() + "user/passwordReset").request().post(Entity.json(req), Void.TYPE);

		List<MailMessage> mails = mailbox.getMailMessagesSentTo("admin@commafeed.com");
		Assertions.assertEquals(1, mails.size());

		MailMessage message = mails.get(0);
		Assertions.assertEquals("CommaFeed - Password recovery", message.getSubject());
		Assertions.assertTrue(message.getHtml().startsWith("You asked for password recovery for account 'admin'"));
		Assertions.assertEquals("admin@commafeed.com", message.getTo().get(0));
	}
}
