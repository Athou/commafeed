package com.commafeed.frontend.pages;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;

import com.commafeed.frontend.pages.auth.Role;

@SuppressWarnings("serial")
@AuthorizeInstantiation(Role.USER)
public class HomePage extends WebPage {

}
