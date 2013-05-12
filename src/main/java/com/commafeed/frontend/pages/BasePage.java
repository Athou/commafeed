package com.commafeed.frontend.pages;

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;

import com.commafeed.backend.StartupBean;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.ApplicationSettings;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.frontend.CommaFeedSession;
import com.commafeed.frontend.utils.WicketUtils;
import com.google.api.client.util.Maps;

@SuppressWarnings("serial")
public abstract class BasePage extends WebPage {

	@Inject
	protected FeedDAO feedDAO;

	@Inject
	StartupBean startupBean;

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

		String lang = "en";
		User user = CommaFeedSession.get().getUser();
		if (user != null) {
			UserSettings settings = userSettingsDAO.findByUser(user);
			if (settings != null) {
				lang = settings.getLanguage() == null ? "en" : settings
						.getLanguage();
			}
		}

		add(new TransparentWebMarkupContainer("html")
				.add(new AttributeModifier("lang", lang)));

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
			long startupTime = startupBean.getStartupTime();
			String suffix = "?" + startupTime;
			response.render(JavaScriptHeaderItem.forUrl("wro/all.js" + suffix));
			response.render(CssHeaderItem.forUrl("wro/all.css" + suffix));
		} else {
			response.render(JavaScriptHeaderItem.forUrl("wro/lib.js"));
			response.render(CssHeaderItem.forUrl("wro/lib.css"));
			response.render(CssHeaderItem.forUrl("wro/app.css"));

			response.render(JavaScriptHeaderItem.forUrl("js/welcome.js"));
			response.render(JavaScriptHeaderItem.forUrl("js/main.js"));
			response.render(JavaScriptHeaderItem.forUrl("js/controllers.js"));
			response.render(JavaScriptHeaderItem.forUrl("js/directives.js"));
			response.render(JavaScriptHeaderItem.forUrl("js/filters.js"));
			response.render(JavaScriptHeaderItem.forUrl("js/services.js"));

		}

		if (StringUtils.isNotBlank(settings.getGoogleAnalyticsTrackingCode())) {
			Map<String, Object> vars = Maps.newHashMap();
			vars.put("trackingCode", settings.getGoogleAnalyticsTrackingCode());
			WicketUtils.loadJS(response, BasePage.class, "analytics", vars);
		}

	}
}
