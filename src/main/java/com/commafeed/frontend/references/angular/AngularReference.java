package com.commafeed.frontend.references.angular;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

@SuppressWarnings("serial")
public class AngularReference extends WebjarsJavaScriptResourceReference {

	private static AngularReference instance = new AngularReference();

	public AngularReference() {
		super("angularjs/current/angular.min.js");
	}

	public static void render(IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(instance));
	}
}
