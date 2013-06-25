package com.commafeed.frontend.pages;

import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;

import com.commafeed.backend.model.User;
import com.commafeed.backend.services.PasswordEncryptionService;
import com.commafeed.backend.services.UserService;
import com.commafeed.frontend.pages.components.BootstrapFeedbackPanel;
import com.commafeed.frontend.utils.exception.DisplayException;

@SuppressWarnings("serial")
public class PasswordRecoveryCallbackPage extends BasePage {

	public static final String PARAM_EMAIL = "email";
	public static final String PARAM_TOKEN = "token";

	@Inject
	PasswordEncryptionService encryptionService;

	@Inject
	UserService userService;

	public PasswordRecoveryCallbackPage(PageParameters params) {
		String email = params.get(PARAM_EMAIL).toString();
		String token = params.get(PARAM_TOKEN).toString();

		final User user = userDAO.findByEmail(email);
		if (user == null) {
			throw new DisplayException("email not found");
		}
		if (user.getRecoverPasswordToken() == null
				|| !user.getRecoverPasswordToken().equals(token)) {
			throw new DisplayException("invalid token");
		}
		if (user.getRecoverPasswordTokenDate().before(
				DateUtils.addDays(new Date(), -2))) {
			throw new DisplayException("token expired");
		}

		final IModel<String> password = new Model<String>();
		final IModel<String> confirm = new Model<String>();
		add(new BootstrapFeedbackPanel("feedback"));
		Form<Void> form = new Form<Void>("form") {
			@Override
			protected void onSubmit() {
				String passwd = password.getObject();
				if (StringUtils.equals(passwd, confirm.getObject())) {
					byte[] password = encryptionService.getEncryptedPassword(
							passwd, user.getSalt());
					user.setPassword(password);
					user.setApiKey(userService.generateApiKey(user));
					user.setRecoverPasswordToken(null);
					user.setRecoverPasswordTokenDate(null);
					userDAO.saveOrUpdate(user);
					info("Password saved.");
				} else {
					error("Passwords do not match.");
				}
			}
		};
		add(form);
		form.add(new PasswordTextField("password", password).setResetPassword(
				true).add(StringValidator.minimumLength(6)));
		form.add(new PasswordTextField("confirm", confirm).setResetPassword(
				true).add(StringValidator.minimumLength(6)));

		form.add(new BookmarkablePageLink<Void>("cancel", HomePage.class));

	}
}
