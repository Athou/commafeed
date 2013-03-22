package com.commafeed.frontend.rest;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.commafeed.frontend.CommaFeedSession;
import com.commafeed.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@SuppressWarnings("serial")
public abstract class JSONPage extends WebPage {

	public JSONPage(PageParameters pageParameters) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
		getRequestCycle().scheduleRequestHandlerAfterCurrent(
				new TextRequestHandler("application/json", "UTF-8", gson
						.toJson(getObject(pageParameters))));
	}

	protected abstract Object getObject(PageParameters parameters);

	protected User getUser() {
		return CommaFeedSession.get().getUser();
	}
}
