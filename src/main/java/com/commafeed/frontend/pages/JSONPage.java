package com.commafeed.frontend.pages;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public abstract class JSONPage extends WebPage {

	public JSONPage() {
		this(new PageParameters());
	}

	public JSONPage(PageParameters pageParameters) {
		getRequestCycle().scheduleRequestHandlerAfterCurrent(
				new TextRequestHandler("application/json", "UTF-8", new Gson()
						.toJson(getObject())));
	}

	protected abstract Object getObject();

}
