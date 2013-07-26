package com.commafeed.frontend.pages.components;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.authroles.authentication.panel.SignInPanel;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.frontend.pages.PasswordRecoveryPage;

@SuppressWarnings("serial")
public class LoginPanel extends SignInPanel {

	@Inject
	ApplicationSettingsService applicationSettingsService;

	public LoginPanel(String id) {
		super(id);
		replace(new BootstrapFeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this)));
		Form<?> form = (Form<?>) get("signInForm");
		form.add(new BookmarkablePageLink<Void>("recover", PasswordRecoveryPage.class) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				String smtpHost = applicationSettingsService.get().getSmtpHost();
				setVisibilityAllowed(StringUtils.isNotBlank(smtpHost));
			}
		});
	}

}
