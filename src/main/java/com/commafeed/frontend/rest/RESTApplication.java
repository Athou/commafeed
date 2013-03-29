package com.commafeed.frontend.rest;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.commafeed.frontend.rest.resources.AdminREST;
import com.commafeed.frontend.rest.resources.EntriesREST;
import com.commafeed.frontend.rest.resources.SettingsREST;
import com.commafeed.frontend.rest.resources.SubscriptionsREST;
import com.google.common.collect.Sets;

@ApplicationPath("/rest")
public class RESTApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> set = Sets.newHashSet();
		set.add(JSONMessageBodyReaderWriter.class);

		set.add(SubscriptionsREST.class);
		set.add(EntriesREST.class);
		set.add(SettingsREST.class);
		set.add(AdminREST.class);
		return set;
	}
}
