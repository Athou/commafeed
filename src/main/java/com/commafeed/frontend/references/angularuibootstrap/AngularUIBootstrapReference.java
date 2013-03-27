package com.commafeed.frontend.references.angularuibootstrap;

import java.util.Arrays;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.commafeed.frontend.references.angular.AngularReference;

public class AngularUIBootstrapReference extends JavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	public static final AngularUIBootstrapReference INSTANCE = new AngularUIBootstrapReference();

	private AngularUIBootstrapReference() {
		super(AngularUIBootstrapReference.class, "ui-bootstrap-tpls-0.2.0.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays.asList(JavaScriptHeaderItem
				.forReference(AngularReference.INSTANCE));
	}

	public static void renderHead(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(INSTANCE));
	}
}