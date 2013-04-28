package com.commafeed.frontend.resources;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.extensions.processor.css.RubySassCssProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;

@SupportedResourceType(ResourceType.CSS)
public class SassOnlyProcessor extends RubySassCssProcessor {

	@Override
	public void process(Resource resource, Reader reader, Writer writer)
			throws IOException {
		if (resource.getUri().endsWith(".sass")
				|| resource.getUri().endsWith(".scss")) {
			super.process(resource, reader, writer);
		} else {
			writer.write(IOUtils.toString(reader));
		}
	}

}
