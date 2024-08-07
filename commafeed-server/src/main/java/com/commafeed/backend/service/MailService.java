package com.commafeed.backend.service;

import java.util.Optional;
import java.util.Properties;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.CommaFeedConfiguration.Smtp;
import com.commafeed.backend.model.User;

import jakarta.inject.Singleton;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

/**
 * Mailing service
 * 
 */
@RequiredArgsConstructor
@Singleton
public class MailService {

	private final CommaFeedConfiguration config;

	public void sendMail(User user, String subject, String content) throws Exception {
		Optional<Smtp> settings = config.smtp();
		if (settings.isEmpty()) {
			throw new IllegalArgumentException("SMTP settings not configured");
		}

		final String username = settings.get().userName();
		final String password = settings.get().password();
		final String fromAddress = Optional.ofNullable(settings.get().fromAddress()).orElse(settings.get().userName());

		String dest = user.getEmail();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", String.valueOf(settings.get().tls()));
		props.put("mail.smtp.host", settings.get().host());
		props.put("mail.smtp.port", String.valueOf(settings.get().port()));

		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(fromAddress, "CommaFeed"));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(dest));
		message.setSubject("CommaFeed - " + subject);
		message.setContent(content, "text/html; charset=utf-8");

		Transport.send(message);

	}
}
