package com.commafeed.frontend.references.angular;

import java.util.Arrays;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

public class AngularSanitizeReference extends UrlResourceReference {
	private static final long serialVersionUID = 1L;

	public static final AngularSanitizeReference INSTANCE = new AngularSanitizeReference();

	private AngularSanitizeReference() {
		super(
				Url.parse("https://ajax.googleapis.com/ajax/libs/angularjs/1.1.4/angular-sanitize.min.js"));
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