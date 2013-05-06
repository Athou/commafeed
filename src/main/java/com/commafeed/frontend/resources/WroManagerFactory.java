package com.commafeed.frontend.resources;

import java.util.Map;

import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

public class WroManagerFactory extends ConfigurableWroManagerFactory {

	@Override
	protected void contributePreProcessors(Map<String, ResourcePreProcessor> map) {
		map.put("sassOnlyProcessor", new SassOnlyProcessor());
		map.put("sassImport", new SassImportProcessor());
	}

}
