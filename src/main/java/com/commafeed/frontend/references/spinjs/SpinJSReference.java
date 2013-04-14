package com.commafeed.frontend.references.spinjs;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

public class SpinJSReference extends UrlResourceReference {
	private static final long serialVersionUID = 1L;

	public static final SpinJSReference INSTANCE = new SpinJSReference();

	private SpinJSReference() {
		super(
				Url.parse("https://cdnjs.cloudflare.com/ajax/libs/spin.js/1.2.7/spin.min.js"));
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}
}