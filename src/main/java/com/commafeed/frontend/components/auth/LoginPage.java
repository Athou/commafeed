package com.commafeed.frontend.components.auth;

import org.apache.wicket.markup.html.WebPage;

@SuppressWarnings("serial")
public class LoginPage extends WebPage {

	public LoginPage() {
		add(new LoginPanel("login"));
	}
}
