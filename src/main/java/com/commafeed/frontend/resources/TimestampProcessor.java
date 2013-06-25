package com.commafeed.frontend.resources;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

@SupportedResourceType(ResourceType.JS)
public class TimestampProcessor implements ResourcePreProcessor {

	private static final String NOW = "" + System.currentTimeMillis();

	@Override
	public void process(Resource resource, Reader reader, Writer writer)
			throws IOException {
		String content = IOUtils.toString(reader);
		content = content.replace("${timestamp}", NOW);
		writer.write(content);
	}

}
