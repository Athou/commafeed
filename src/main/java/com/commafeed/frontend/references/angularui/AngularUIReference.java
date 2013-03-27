package com.commafeed.frontend.references.angularui;

import java.util.Arrays;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.commafeed.frontend.utils.WicketUtils;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

public class AngularUIReference extends WebjarsJavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	public static final AngularUIReference INSTANCE = new AngularUIReference();

	private AngularUIReference() {
		super("/angular-ui/current/angular-ui.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays
				.asList(WicketUtils
						.buildCssWebJarHeaderItem("/angular-ui/current/angular-ui.css"));
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}
}