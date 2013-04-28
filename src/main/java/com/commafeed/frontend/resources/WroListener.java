package com.commafeed.frontend.resources;

import java.util.ResourceBundle;

import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.WroServletContextListener;

public class WroListener extends WroServletContextListener {

	@Override
	protected WroConfiguration newConfiguration() {
		WroConfiguration conf = super.newConfiguration();

		boolean prod = Boolean.valueOf(ResourceBundle.getBundle("application")
				.getString("production"));

		conf.setResourceWatcherUpdatePeriod(prod ? 0 : 1);
		conf.setDisableCache(!prod);
		conf.setDebug(!prod);
		return conf;
	}

}
