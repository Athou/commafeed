package com.commafeed.frontend.references.select2;

import java.util.Arrays;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.commafeed.frontend.utils.WicketUtils;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

public class Select2Reference extends WebjarsJavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	public static final Select2Reference INSTANCE = new Select2Reference();

	private Select2Reference() {
		super("/select2/current/select2.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays.asList(WicketUtils
				.buildCssWebJarHeaderItem("/select2/current/select2.css"));
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}
}