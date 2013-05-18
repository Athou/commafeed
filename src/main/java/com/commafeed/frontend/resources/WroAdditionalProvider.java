package com.commafeed.frontend.resources;

import java.util.Map;

import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.ProcessorProvider;

import com.google.api.client.util.Maps;

/**
 * Build-time solution
 *
 */
public class WroAdditionalProvider implements ProcessorProvider {

	@Override
	public Map<String, ResourcePreProcessor> providePreProcessors() {
		Map<String, ResourcePreProcessor> map = Maps.newHashMap();
		map.put("sassOnlyProcessor", new SassOnlyProcessor());
		map.put("sassImport", new SassImportProcessor());
		map.put("timestamp", new TimestampProcessor());
		return map;
	}

	@Override
	public Map<String, ResourcePostProcessor> providePostProcessors() {
		return Maps.newHashMap();
	}

}
