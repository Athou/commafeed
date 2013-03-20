package com.commafeed.frontend.components.auth;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;

import de.agilecoders.wicket.Bootstrap;

@SuppressWarnings("serial")
public class LoginPage extends WebPage {

	public LoginPage() {
		add(new LoginPanel("login"));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		Bootstrap.renderHead(response);
	}

}
