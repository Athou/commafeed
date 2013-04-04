package com.commafeed.frontend.references.angularui;

import java.util.Arrays;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.commafeed.frontend.references.angular.AngularReference;

public class AngularUIReference extends JavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	public static final AngularUIReference INSTANCE = new AngularUIReference();

	private AngularUIReference() {
		super(AngularUIReference.class, "angular-ui.js");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays.asList(JavaScriptHeaderItem
				.forReference(AngularReference.INSTANCE), CssHeaderItem
				.forReference(new CssResourceReference(
						AngularUIReference.class, "angular-ui.css")));
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}
}