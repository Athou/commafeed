package com.commafeed.frontend.pages;

import javax.inject.Inject;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;

import com.commafeed.backend.dao.FeedCategoryService;
import com.commafeed.backend.dao.FeedEntryService;
import com.commafeed.backend.dao.FeedEntryStatusService;
import com.commafeed.backend.dao.FeedService;
import com.commafeed.backend.dao.FeedSubscriptionService;
import com.commafeed.backend.dao.UserRoleService;
import com.commafeed.backend.dao.UserService;
import com.commafeed.backend.dao.UserSettingsService;

import de.agilecoders.wicket.Bootstrap;

@SuppressWarnings("serial")
public class BasePage extends WebPage {

	@Inject
	protected FeedService feedService;

	@Inject
	protected FeedSubscriptionService feedSubscriptionService;

	@Inject
	protected FeedCategoryService feedCategoryService;

	@Inject
	protected FeedEntryService feedEntryService;

	@Inject
	protected FeedEntryStatusService feedEntryStatusService;

	@Inject
	protected UserService userService;

	@Inject
	protected UserSettingsService userSettingsService;

	@Inject
	protected UserRoleService userRoleService;

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		Bootstrap.renderHead(response);
	}
}
