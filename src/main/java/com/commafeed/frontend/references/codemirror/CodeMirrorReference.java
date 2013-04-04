package com.commafeed.frontend.references.codemirror;

import java.util.Arrays;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.commafeed.frontend.utils.WicketUtils;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

public class CodeMirrorReference extends WebjarsJavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	public static final CodeMirrorReference INSTANCE = new CodeMirrorReference();

	private CodeMirrorReference() {
		super("/codemirror/current/lib/codemirror.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays
				.asList(WicketUtils
						.buildCssWebJarHeaderItem("/codemirror/current/lib/codemirror.css"));
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}
}