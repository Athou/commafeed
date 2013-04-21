package com.commafeed.frontend.references.fontawesome;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

public class FontAwesomeReference extends UrlResourceReference {

	private static final long serialVersionUID = 1L;

	public static final FontAwesomeReference INSTANCE = new FontAwesomeReference();

	public FontAwesomeReference() {
		super(
				Url.parse("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/3.0.2/css/font-awesome.min.css"));
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(CssHeaderItem.forReference(INSTANCE));
	}

}
