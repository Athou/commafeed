package com.commafeed.frontend.references.mousetrap;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

public class MouseTrapReference extends WebjarsJavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	public static final MouseTrapReference INSTANCE = new MouseTrapReference();

	private MouseTrapReference() {
		super("/mousetrap/current/mousetrap.js");
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}
}