package com.commafeed.frontend.references.codemirror;

import java.util.Arrays;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

public class CodeMirrorReference extends UrlResourceReference {
	private static final long serialVersionUID = 1L;

	public static final CodeMirrorReference INSTANCE = new CodeMirrorReference();

	private CodeMirrorReference() {
		super(
				Url.parse("https://cdnjs.cloudflare.com/ajax/libs/codemirror/2.36.0/codemirror.min.js"));
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays
				.asList(CssHeaderItem.forReference(new UrlResourceReference(
						Url.parse("https://cdnjs.cloudflare.com/ajax/libs/codemirror/2.36.0/codemirror.min.css"))));
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}
}