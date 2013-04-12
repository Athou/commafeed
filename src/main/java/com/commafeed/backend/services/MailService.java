package com.commafeed.backend.services;

import java.util.Properties;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.commafeed.backend.model.ApplicationSettings;
import com.commafeed.backend.model.User;

public class MailService {

	@Inject
	ApplicationSettingsService applicationSettingsService;

	public void sendMail(User user, String subject, String content) throws Exception {

		ApplicationSettings settings = applicationSettingsService.get();
		
		final String username = settings.getSmtpUserName();
		final String password = settings.getSmtpPassword();
		
		String dest = user.getEmail();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", settings.isSmtpTls());
		props.put("mail.smtp.host", settings.getSmtpHost());
		props.put("mail.smtp.port", settings.getSmtpPort());

		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(username, "CommaFeed"));
		message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(dest));
		message.setSubject("CommaFeed - " + subject);
		message.setText(content);

		Transport.send(message);

	}
}
