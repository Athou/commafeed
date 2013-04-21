package com.commafeed.frontend.references.bootstrap;

import java.util.Arrays;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

import com.commafeed.frontend.references.jquery.JQueryReference;

public class BootstrapReference extends UrlResourceReference {

	private static final long serialVersionUID = 1L;

	public static final BootstrapReference INSTANCE = new BootstrapReference();

	public BootstrapReference() {
		super(
				Url.parse("https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.0/bootstrap.min.js"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays
				.asList(JavaScriptHeaderItem
						.forReference(JQueryReference.INSTANCE),
						CssHeaderItem.forReference(new UrlResourceReference(
								Url.parse("https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.0/css/bootstrap.no-icons.min.css"))),
						CssHeaderItem.forReference(new UrlResourceReference(
								Url.parse("https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.0/css/bootstrap-responsive.min.css"))));
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}

}
