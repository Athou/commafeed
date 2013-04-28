package com.commafeed.frontend.pages;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.frontend.CommaFeedSession;
import com.commafeed.frontend.SecurityCheck;
import com.commafeed.frontend.resources.UserCustomCssReference;

@SuppressWarnings("serial")
@SecurityCheck(Role.USER)
public class HomePage extends BasePage {

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(CssHeaderItem.forReference(
				new UserCustomCssReference() {
					@Override
					protected String getCss() {
						UserSettings settings = userSettingsDAO
								.findByUser(CommaFeedSession.get().getUser());
						return settings == null ? null : settings
								.getCustomCss();
					}
				}, new PageParameters().add("_t", System.currentTimeMillis()),
				null));
	}
}
