package com.commafeed.frontend.rest.resources;

import java.lang.reflect.Method;
import java.util.Set;

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
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebResponse;
import org.apache.wicket.request.cycle.RequestCycle;

import com.commafeed.backend.dao.FeedCategoryService;
import com.commafeed.backend.dao.FeedEntryService;
import com.commafeed.backend.dao.FeedEntryStatusService;
import com.commafeed.backend.dao.FeedService;
import com.commafeed.backend.dao.FeedSubscriptionService;
import com.commafeed.backend.dao.UserRoleService;
import com.commafeed.backend.dao.UserService;
import com.commafeed.backend.dao.UserSettingsService;
import com.commafeed.backend.feeds.OPMLImporter;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.security.PasswordEncryptionService;
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
	FeedService feedService;

	@Inject
	FeedSubscriptionService feedSubscriptionService;

	@Inject
	FeedCategoryService feedCategoryService;

	@Inject
	FeedEntryService feedEntryService;

	@Inject
	FeedEntryStatusService feedEntryStatusService;

	@Inject
	UserService userService;

	@Inject
	UserSettingsService userSettingsService;

	@Inject
	UserRoleService userRoleService;

	@Inject
	OPMLImporter opmlImporter;

	@Inject
	PasswordEncryptionService encryptionService;

	@PostConstruct
	public void init() {
		CommaFeedApplication app = CommaFeedApplication.get();
		ServletWebRequest swreq = new ServletWebRequest(request, "");
		ServletWebResponse swresp = new ServletWebResponse(swreq, response);
		RequestCycle cycle = app.createRequestCycle(swreq, swresp);
		ThreadContext.setRequestCycle(cycle);
		CommaFeedSession session = (CommaFeedSession) app
				.fetchCreateAndSetSession(cycle);

		IAuthenticationStrategy authenticationStrategy = app
				.getSecuritySettings().getAuthenticationStrategy();
		String[] data = authenticationStrategy.load();
		if (data != null && data.length > 1) {
			session.signIn(data[0], data[1]);
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
		Set<Role> roles = userRoleService.getRoles(user);
		if (!roles.contains(annotation.value())) {
			return false;
		}
		return true;
	}

}
