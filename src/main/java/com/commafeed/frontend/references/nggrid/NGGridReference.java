package com.commafeed.frontend.references.nggrid;

import java.util.Arrays;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.commafeed.frontend.references.angular.AngularReference;

public class NGGridReference extends JavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	public static final NGGridReference INSTANCE = new NGGridReference();

	private NGGridReference() {
		super(NGGridReference.class, "ng-grid-2.0.2.js");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays.asList(JavaScriptHeaderItem
				.forReference(AngularReference.INSTANCE), CssHeaderItem
				.forReference(new CssResourceReference(NGGridReference.class,
						"ng-grid.css")));
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}
}