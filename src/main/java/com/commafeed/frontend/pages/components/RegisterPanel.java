package com.commafeed.frontend.pages.components;

import java.util.Arrays;

import javax.inject.Inject;

import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.extensions.validation.validator.RfcCompliantEmailAddressValidator;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;

import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.backend.services.UserService;
import com.commafeed.frontend.CommaFeedSession;
import com.commafeed.frontend.model.request.RegistrationRequest;
import com.commafeed.frontend.utils.ModelFactory.MF;

@SuppressWarnings("serial")
public class RegisterPanel extends Panel {

	@Inject
	UserDAO userDAO;

	@Inject
	UserService userService;

	@Inject
	ApplicationSettingsService applicationSettingsService;

	public RegisterPanel(String markupId) {
		super(markupId);

		IModel<RegistrationRequest> model = Model.of(new RegistrationRequest());

		Form<RegistrationRequest> form = new StatelessForm<RegistrationRequest>(
				"form", model) {
			@Override
			protected void onSubmit() {
				if (applicationSettingsService.get().isAllowRegistrations()) {
					RegistrationRequest req = getModelObject();
					userService.register(req.getName(), req.getPassword(),
							req.getEmail(), Arrays.asList(Role.USER));

					IAuthenticationStrategy strategy = getApplication()
							.getSecuritySettings().getAuthenticationStrategy();
					strategy.save(req.getName(), req.getPassword());
					CommaFeedSession.get().signIn(req.getName(),
							req.getPassword());
				}
				setResponsePage(getApplication().getHomePage());
			}
		};
		add(form);
		add(new BootstrapFeedbackPanel("feedback",
				new ContainerFeedbackMessageFilter(form)));

		RegistrationRequest p = MF.p(RegistrationRequest.class);
		form.add(new RequiredTextField<String>("name", MF.m(model, p.getName()))
				.add(StringValidator.lengthBetween(3, 32)).add(
						new IValidator<String>() {
							@Override
							public void validate(
									IValidatable<String> validatable) {
								String name = validatable.getValue();
								User user = userDAO.findByName(name);
								if (user != null) {
									validatable.error(new ValidationError(
											"Name is already taken."));
								}
							}
						}));
		form.add(new PasswordTextField("password", MF.m(model, p.getPassword()))
				.setResetPassword(false).add(StringValidator.minimumLength(6)));
		form.add(new RequiredTextField<String>("email", MF.m(model,
				p.getEmail())) {
			@Override
			protected String getInputType() {
				return "email";
			}
		}.add(RfcCompliantEmailAddressValidator.getInstance()).add(
				new IValidator<String>() {
					@Override
					public void validate(IValidatable<String> validatable) {
						String email = validatable.getValue();
						User user = userDAO.findByEmail(email);
						if (user != null) {
							validatable.error(new ValidationError(
									"Email is already taken."));
						}
					}
				}));
	}
}
