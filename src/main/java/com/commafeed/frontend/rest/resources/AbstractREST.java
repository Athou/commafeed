package com.commafeed.frontend.rest.resources;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebResponse;
import org.apache.wicket.request.cycle.RequestCycle;

import com.commafeed.backend.dao.FeedCategoryService;
import com.commafeed.backend.dao.FeedEntryService;
import com.commafeed.backend.dao.FeedEntryStatusService;
import com.commafeed.backend.dao.FeedSubscriptionService;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.CommaFeedSession;

@Produces(MediaType.APPLICATION_JSON)
public abstract class AbstractREST {

	@Context
	HttpServletRequest request;

	@Context
	HttpServletResponse response;

	@Inject
	FeedSubscriptionService feedSubscriptionService;

	@Inject
	FeedCategoryService feedCategoryService;

	@Inject
	FeedEntryService feedEntryService;

	@Inject
	FeedEntryStatusService feedEntryStatusService;

	@PostConstruct
	public void init() {
		ServletWebRequest swreq = new ServletWebRequest(request, "");
		ServletWebResponse swresp = new ServletWebResponse(swreq, response);
		RequestCycle cycle = Application.get()
				.createRequestCycle(swreq, swresp);
		ThreadContext.setRequestCycle(cycle);
		Application.get().fetchCreateAndSetSession(
				Application.get().createRequestCycle(swreq, swresp));
	}

	protected User getUser() {
		return CommaFeedSession.get().getUser();
	}

}
