package com.commafeed.frontend.pages;

import java.util.Calendar;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.wicket.extensions.validation.validator.RfcCompliantEmailAddressValidator;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.model.User;
import com.commafeed.frontend.pages.components.BootstrapFeedbackPanel;

@SuppressWarnings("serial")
public class PasswordRecoveryPage extends BasePage {

	private static Logger log = LoggerFactory
			.getLogger(PasswordRecoveryPage.class);

	public PasswordRecoveryPage() {

		IModel<String> email = new Model<String>();
		add(new BootstrapFeedbackPanel("feedback"));
		Form<String> form = new Form<String>("form", email) {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				User user = userDAO.findByEmail(getModelObject());
				if (user == null) {
					error("Email not found.");
				} else {
					try {
						user.setRecoverPasswordToken(DigestUtils.sha1Hex(UUID
								.randomUUID().toString()));
						user.setRecoverPasswordTokenDate(Calendar.getInstance()
								.getTime());
						userDAO.update(user);
						mailService.sendMail(user,
								"CommaFeed - Password recovery",
								buildEmailContent(user));
						info("Email sent.");
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						error("Cannot send email, please contact the staff.");
					}

				}
			}

		};
		add(form);

		form.add(new RequiredTextField<String>("email", email) {
			@Override
			protected String getInputType() {
				return "email";
			}
		}.add(RfcCompliantEmailAddressValidator.getInstance()));

		form.add(new BookmarkablePageLink<Void>("cancel", HomePage.class));
	}

	private String buildEmailContent(User user) {
		return "cc";
	}
}
