package com.commafeed.frontend.pages;

import javax.inject.Inject;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.frontend.pages.components.LoginPanel;
import com.commafeed.frontend.pages.components.RegisterPanel;

@SuppressWarnings("serial")
public class WelcomePage extends BasePage {

	@Inject
	ApplicationSettingsService applicationSettingsService;

	public WelcomePage() {
		add(new BookmarkablePageLink<Void>("logo-link", getApplication().getHomePage()));
		add(new BookmarkablePageLink<Void>("demo-login", DemoLoginPage.class));
		add(new LoginPanel("login"));
		add(new RegisterPanel("register") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisibilityAllowed(applicationSettingsService.get().isAllowRegistrations());
			}
		});
	}
}
