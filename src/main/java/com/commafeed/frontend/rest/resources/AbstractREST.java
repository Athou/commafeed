package com.commafeed.frontend.rest.resources;

import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebResponse;
import org.apache.wicket.request.cycle.RequestCycle;

import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.feeds.FeedFetcher;
import com.commafeed.backend.feeds.OPMLImporter;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.services.FeedSubscriptionService;
import com.commafeed.backend.services.PasswordEncryptionService;
import com.commafeed.backend.services.UserService;
import com.commafeed.frontend.CommaFeedApplication;
import com.commafeed.frontend.CommaFeedSession;
import com.commafeed.frontend.SecurityCheck;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityCheck(Role.USER)
public abstract class AbstractREST {

	@Context
	HttpServletRequest request;

	@Context
	HttpServletResponse response;

	@Inject
	FeedDAO feedDAO;

	@Inject
	FeedSubscriptionDAO feedSubscriptionDAO;

	@Inject
	FeedSubscriptionService feedSubscriptionService;

	@Inject
	FeedCategoryDAO feedCategoryDAO;

	@Inject
	FeedEntryDAO feedEntryDAO;

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	@Inject
	UserDAO userDAO;

	@Inject
	UserService userService;

	@Inject
	UserSettingsDAO userSettingsDAO;

	@Inject
	UserRoleDAO userRoleDAO;

	@Inject
	OPMLImporter opmlImporter;

	@Inject
	PasswordEncryptionService encryptionService;

	@Inject
	FeedFetcher feedFetcher;

	@PostConstruct
	public void init() {
		CommaFeedApplication app = CommaFeedApplication.get();
		ServletWebRequest swreq = new ServletWebRequest(request, "");
		ServletWebResponse swresp = new ServletWebResponse(swreq, response);
		RequestCycle cycle = app.createRequestCycle(swreq, swresp);
		ThreadContext.setRequestCycle(cycle);
		CommaFeedSession session = (CommaFeedSession) app
				.fetchCreateAndSetSession(cycle);

		if (session.getUser() == null) {
			IAuthenticationStrategy authenticationStrategy = app
					.getSecuritySettings().getAuthenticationStrategy();
			String[] data = authenticationStrategy.load();
			if (data != null && data.length > 1) {
				session.signIn(data[0], data[1]);
			}
		}

	}

	protected User getUser() {
		return CommaFeedSession.get().getUser();
	}

	@AroundInvoke
	public Object checkSecurity(InvocationContext context) throws Exception {
		User user = getUser();
		if (user == null) {
			throw new WebApplicationException(Response
					.status(Status.UNAUTHORIZED)
					.entity("You need to be authenticated to do this.").build());
		}

		boolean allowed = false;
		Method method = context.getMethod();

		if (method.isAnnotationPresent(SecurityCheck.class)) {
			allowed = checkRole(user, method.getAnnotation(SecurityCheck.class));
		} else if (method.getDeclaringClass().isAnnotationPresent(
				SecurityCheck.class)) {
			allowed = checkRole(
					user,
					method.getDeclaringClass().getAnnotation(
							SecurityCheck.class));
		}
		if (!allowed) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("You are not authorized to do this.").build());
		}

		return context.proceed();
	}

	private boolean checkRole(User user, SecurityCheck annotation) {
		Roles roles = CommaFeedSession.get().getRoles();
		boolean authorized = roles.hasAnyRole(new Roles(annotation.value()
				.name()));
		return authorized;
	}

}
