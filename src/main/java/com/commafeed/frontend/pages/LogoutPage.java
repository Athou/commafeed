package com.commafeed.frontend.pages;

import org.apache.wicket.markup.html.WebPage;

@SuppressWarnings("serial")
public class LogoutPage extends WebPage {
	public LogoutPage() {
		getSession().invalidate();
		setResponsePage(getApplication().getHomePage());
	}
}
