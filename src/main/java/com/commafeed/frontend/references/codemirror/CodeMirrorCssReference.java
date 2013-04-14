package com.commafeed.frontend.references.codemirror;

import java.util.Arrays;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

public class CodeMirrorCssReference extends UrlResourceReference {
	private static final long serialVersionUID = 1L;

	public static final CodeMirrorCssReference INSTANCE = new CodeMirrorCssReference();

	private CodeMirrorCssReference() {
		super(
				Url.parse("https://cdnjs.cloudflare.com/ajax/libs/codemirror/2.36.0/css.js"));
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays.asList(JavaScriptHeaderItem
				.forReference(CodeMirrorReference.INSTANCE));
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}
}