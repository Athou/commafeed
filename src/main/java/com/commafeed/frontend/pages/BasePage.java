package com.commafeed.frontend.pages;

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;

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
import com.commafeed.frontend.utils.WicketUtils;
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

	private ApplicationSettings settings;

	public BasePage() {
		settings = applicationSettingsService.get();
		add(new HeaderResponseContainer("footer-container", "footer-container"));
		add(new WebMarkupContainer("uservoice") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisibilityAllowed(settings.isFeedbackButton());
			}
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		if (getApplication().getConfigurationType() == RuntimeConfigurationType.DEPLOYMENT) {
			response.render(JavaScriptHeaderItem.forUrl("wro/all.js"));
			response.render(CssHeaderItem.forUrl("wro/all.css"));
		} else {
			response.render(JavaScriptHeaderItem.forUrl("wro/lib.js"));
			response.render(CssHeaderItem.forUrl("wro/lib.css"));

			response.render(JavaScriptHeaderItem.forUrl("js/welcome.js"));
			response.render(JavaScriptHeaderItem.forUrl("js/main.js"));
			response.render(JavaScriptHeaderItem.forUrl("js/controllers.js"));
			response.render(JavaScriptHeaderItem.forUrl("js/directives.js"));
			response.render(JavaScriptHeaderItem.forUrl("js/services.js"));
			response.render(CssHeaderItem.forUrl("css/app.css"));
		}

		if (StringUtils.isNotBlank(settings.getGoogleAnalyticsTrackingCode())) {
			Map<String, Object> vars = Maps.newHashMap();
			vars.put("trackingCode", settings.getGoogleAnalyticsTrackingCode());
			WicketUtils.loadJS(response, BasePage.class, "analytics", vars);
		}

	}
}
