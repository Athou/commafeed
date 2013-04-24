package com.commafeed.frontend.pages;

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.resource.TextTemplateResourceReference;

import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.ApplicationSettings;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.frontend.references.bootstrap.BootstrapReference;
import com.google.api.client.util.Maps;

@SuppressWarnings("serial")
public abstract class BasePage extends WebPage {

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

	@Inject
	ApplicationSettingsService applicationSettingsService;

	public BasePage() {
		add(new HeaderResponseContainer("footer-container", "footer-container"));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		BootstrapReference.renderHead(response);

		final ApplicationSettings settings = applicationSettingsService.get();
		if (StringUtils.isNotBlank(settings.getGoogleAnalyticsTrackingCode())) {
			Map<String, Object> vars = Maps.newHashMap();
			vars.put("trackingCode", settings.getGoogleAnalyticsTrackingCode());
			response.render(JavaScriptHeaderItem
					.forReference(new TextTemplateResourceReference(
							BasePage.class, "analytics.js", Model.ofMap(vars))));

		}
	}
}
