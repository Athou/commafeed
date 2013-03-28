package com.commafeed.frontend.pages;

import com.commafeed.frontend.pages.auth.LoginPanel;

@SuppressWarnings("serial")
public class LoginPage extends BasePage {

	public LoginPage() {
		add(new LoginPanel("login"));
	}
}
