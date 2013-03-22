package com.commafeed.frontend.rest;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.commafeed.frontend.CommaFeedSession;
import com.commafeed.model.User;
import com.google.gson.Gson;

@SuppressWarnings("serial")
public abstract class JSONPage extends WebPage {

	public JSONPage(PageParameters pageParameters) {
		getRequestCycle().scheduleRequestHandlerAfterCurrent(
				new TextRequestHandler("application/json", "UTF-8", new Gson()
						.toJson(getObject(pageParameters))));
	}

	protected abstract Object getObject(PageParameters parameters);

	protected User getUser() {
		return CommaFeedSession.get().getUser();
	}
}
