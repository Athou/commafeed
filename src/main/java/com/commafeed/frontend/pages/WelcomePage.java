package com.commafeed.frontend.pages;

import javax.inject.Inject;

import com.commafeed.backend.dao.ApplicationSettingsService;
import com.commafeed.frontend.pages.components.LoginPanel;
import com.commafeed.frontend.pages.components.RegisterPanel;

@SuppressWarnings("serial")
public class WelcomePage extends BasePage {

	@Inject
	ApplicationSettingsService applicationSettingsService;

	public WelcomePage() {
		add(new LoginPanel("login"));
		add(new RegisterPanel("register") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisibilityAllowed(applicationSettingsService.get()
						.isAllowRegistrations());
			}
		});
	}
}
