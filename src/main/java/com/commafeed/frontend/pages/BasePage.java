package com.commafeed.frontend.pages;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;

import de.agilecoders.wicket.Bootstrap;

@SuppressWarnings("serial")
public class BasePage extends WebPage {

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		Bootstrap.renderHead(response);
	}
}
