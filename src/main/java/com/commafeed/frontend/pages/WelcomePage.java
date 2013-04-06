package com.commafeed.frontend.pages;

import com.commafeed.frontend.pages.components.LoginPanel;
import com.commafeed.frontend.pages.components.RegisterPanel;

@SuppressWarnings("serial")
public class WelcomePage extends BasePage {

	public WelcomePage() {
		add(new LoginPanel("login"));
		add(new RegisterPanel("register"));
	}
}
