package com.commafeed.frontend.pages.home;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

import com.commafeed.frontend.pages.BasePage;
import com.commafeed.frontend.references.angular.AngularReference;
import com.commafeed.frontend.references.csstree.CssTreeViewReference;

@SuppressWarnings("serial")
public class HomePage extends BasePage {

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		AngularReference.render(response);
		CssTreeViewReference.render(response);
		response.render(CssHeaderItem.forReference(new UrlResourceReference(Url
				.parse("css/app.css"))));

		response.render(JavaScriptHeaderItem
				.forReference(new UrlResourceReference(Url.parse("js/main.js"))));
		response.render(JavaScriptHeaderItem
				.forReference(new UrlResourceReference(Url
						.parse("js/directives.js"))));
		response.render(JavaScriptHeaderItem
				.forReference(new UrlResourceReference(Url
						.parse("js/controllers.js"))));
	}

}
