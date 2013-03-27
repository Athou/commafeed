package com.commafeed.frontend.references.angular;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

public class AngularReference extends WebjarsJavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	public static final AngularReference INSTANCE = new AngularReference();

	private AngularReference() {
		super("/angularjs/current/angular.js");
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}
}