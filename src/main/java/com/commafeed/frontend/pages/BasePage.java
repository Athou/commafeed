package com.commafeed.frontend.pages;

import javax.inject.Inject;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;

import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.dao.UserSettingsDAO;

import de.agilecoders.wicket.Bootstrap;

@SuppressWarnings("serial")
public class BasePage extends WebPage {

	@Inject
	protected FeedDAO feedDAO;

	@Inject
	protected FeedSubscriptionDAO feedSubscriptionDAO;

	@Inject
	protected FeedCategoryDAO feedCategoryDAO;

	@Inject
	protected FeedEntryDAO feedEntryDAO;

	@Inject
	protected FeedEntryStatusDAO feedEntryStatusDAO;

	@Inject
	protected UserDAO userDAO;

	@Inject
	protected UserSettingsDAO userSettingsDAO;

	@Inject
	protected UserRoleDAO userRoleDAO;

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		Bootstrap.renderHead(response);
	}
}
