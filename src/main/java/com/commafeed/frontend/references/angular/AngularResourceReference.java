package com.commafeed.frontend.references.angular;

import java.util.Arrays;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

public class AngularResourceReference extends
		WebjarsJavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	public static final AngularResourceReference INSTANCE = new AngularResourceReference();

	private AngularResourceReference() {
		super("/angularjs/current/angular-resource.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays.asList(JavaScriptHeaderItem
				.forReference(AngularReference.INSTANCE));
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}

}