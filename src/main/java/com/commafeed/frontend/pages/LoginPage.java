package com.commafeed.frontend.pages;

import org.apache.wicket.markup.html.WebPage;

import com.commafeed.frontend.pages.auth.LoginPanel;

@SuppressWarnings("serial")
public class LoginPage extends WebPage {

	public LoginPage() {
		add(new LoginPanel("login"));
	}
}
