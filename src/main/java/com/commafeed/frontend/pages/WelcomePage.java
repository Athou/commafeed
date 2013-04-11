package com.commafeed.frontend.pages;

import javax.inject.Inject;

import org.apache.wicket.markup.head.IHeaderResponse;

import com.commafeed.backend.dao.ApplicationSettingsDAO;
import com.commafeed.frontend.pages.components.LoginPanel;
import com.commafeed.frontend.pages.components.RegisterPanel;
import com.commafeed.frontend.utils.WicketUtils;

@SuppressWarnings("serial")
public class WelcomePage extends BasePage {

	@Inject
	ApplicationSettingsDAO applicationSettingsDAO;

	public WelcomePage() {
		add(new LoginPanel("login"));
		add(new RegisterPanel("register") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisibilityAllowed(applicationSettingsDAO.get()
						.isAllowRegistrations());
			}
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		WicketUtils.loadJS(response, WelcomePage.class);
		WicketUtils.loadCSS(response, WelcomePage.class);
	}
}
