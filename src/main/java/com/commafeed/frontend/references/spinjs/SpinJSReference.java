package com.commafeed.frontend.references.spinjs;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

public class SpinJSReference extends WebjarsJavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	public static final SpinJSReference INSTANCE = new SpinJSReference();

	private SpinJSReference() {
		super("/spin-js/current/spin.js");
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}
}