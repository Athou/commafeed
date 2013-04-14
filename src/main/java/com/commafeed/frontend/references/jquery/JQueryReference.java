package com.commafeed.frontend.references.jquery;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

public class JQueryReference extends UrlResourceReference {

	private static final long serialVersionUID = 1L;

	public static final JQueryReference INSTANCE = new JQueryReference();

	public JQueryReference() {
		super(
				Url.parse("https://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"));
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}
}
