package com.commafeed.frontend.references.codemirror;

import java.util.Arrays;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

public class CodeMirrorCssReference extends WebjarsJavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	public static final CodeMirrorCssReference INSTANCE = new CodeMirrorCssReference();

	private CodeMirrorCssReference() {
		super("/codemirror/current/mode/css/css.js");
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