package com.commafeed.frontend.references.csstreeview;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;

public class CssTreeViewReference extends CssResourceReference {
	private static final long serialVersionUID = 1L;

	public static final CssTreeViewReference INSTANCE = new CssTreeViewReference();

	private CssTreeViewReference() {
		super(CssTreeViewReference.class, "css3-treeview.css");
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(CssHeaderItem.forReference(INSTANCE));
	}
}