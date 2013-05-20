package com.commafeed.frontend.rest;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.commafeed.frontend.rest.resources.AdminREST;
import com.commafeed.frontend.rest.resources.ApiDocumentationREST;
import com.commafeed.frontend.rest.resources.CategoryREST;
import com.commafeed.frontend.rest.resources.EntryREST;
import com.commafeed.frontend.rest.resources.FeedREST;
import com.commafeed.frontend.rest.resources.PubSubHubbubCallbackREST;
import com.commafeed.frontend.rest.resources.ServerREST;
import com.commafeed.frontend.rest.resources.UserREST;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.Sets;
import com.wordnik.swagger.jaxrs.JaxrsApiReader;

@ApplicationPath("/rest")
public class RESTApplication extends Application {

	static {
		JaxrsApiReader.setFormatString("");
	}

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> set = Sets.newHashSet();
		set.add(JacksonJsonProvider.class);

		set.add(EntryREST.class);
		set.add(FeedREST.class);
		set.add(CategoryREST.class);
		set.add(UserREST.class);
		set.add(ServerREST.class);
		set.add(AdminREST.class);

		set.add(ApiDocumentationREST.class);
		set.add(PubSubHubbubCallbackREST.class);

		return set;
	}
}
