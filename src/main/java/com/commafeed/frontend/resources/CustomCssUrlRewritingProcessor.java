package com.commafeed.frontend.resources;

import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;

public class CustomCssUrlRewritingProcessor extends CssUrlRewritingProcessor {

	/**
	 * ignore webjar image replacements since they won't be available at runtime anyway
	 */
	@Override
	protected String replaceImageUrl(String cssUri, String imageUrl) {
		if (cssUri.startsWith("webjar:")) {
			return imageUrl;
		}
		return super.replaceImageUrl(cssUri, imageUrl);
	}

}
