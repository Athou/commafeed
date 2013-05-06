package com.commafeed.frontend.resources;

import java.io.IOException;
import java.util.List;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;

@SupportedResourceType(ResourceType.CSS)
public class SassImportProcessor extends CssImportPreProcessor {

	@Override
	protected String doTransform(String cssContent, List<Resource> foundImports)
			throws IOException {
		for (Resource resource : foundImports) {
			String uri = resource.getUri();
			int lastSlash = uri.lastIndexOf('/');
			String prefix = uri.substring(0, lastSlash);
			String suffix = uri.substring(lastSlash + 1);
			uri = prefix + "/_" + suffix + ".scss";
			resource.setUri(uri);
		}
		return super.doTransform(cssContent, foundImports);
	}
}
