package com.commafeed.frontend.references.csstree;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;

/**
 * http://experiments.wemakesites.net/css3-treeview.html
 * 
 */
public class CssTreeViewReference extends CssResourceReference {
	private static CssTreeViewReference instance = new CssTreeViewReference();

	public CssTreeViewReference() {
		super(CssTreeViewReference.class, "css3-treeview.css");
	}

	public static void render(IHeaderResponse response) {
		response.render(CssHeaderItem.forReference(instance));
	}

	public static CssTreeViewReference get() {
		return instance;
	}
}
