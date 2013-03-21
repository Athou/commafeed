package com.commafeed.frontend.pages;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;

import com.commafeed.frontend.components.auth.Role;

import de.agilecoders.wicket.Bootstrap;

@AuthorizeInstantiation(Role.USER)
@SuppressWarnings("serial")
public abstract class BasePage extends WebPage {

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		Bootstrap.renderHead(response);
	}
}
