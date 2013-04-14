package com.commafeed.frontend.references.mousetrap;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

public class MouseTrapReference extends UrlResourceReference {
	private static final long serialVersionUID = 1L;

	public static final MouseTrapReference INSTANCE = new MouseTrapReference();

	private MouseTrapReference() {
		super(
				Url.parse("https://cdnjs.cloudflare.com/ajax/libs/mousetrap/1.2.2/mousetrap.min.js"));
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}
}