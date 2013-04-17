package com.commafeed.frontend.rest.resources;

import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
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
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.backend.services.FeedSubscriptionService;
import com.commafeed.backend.services.PasswordEncryptionService;
import com.commafeed.backend.services.UserService;
import com.commafeed.frontend.CommaFeedApplication;
import com.commafeed.frontend.CommaFeedSession;
import com.commafeed.frontend.SecurityCheck;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.rest.ApiListingResource;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.core.util.TypeUtil;
import com.wordnik.swagger.jaxrs.HelpApi;
import com.wordnik.swagger.jaxrs.JaxrsApiReader;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityCheck(Role.USER)
public abstract class AbstractREST {

	@Context
	HttpServletRequest request;

	@Context
	HttpServletResponse response;

	@Inject
	ApplicationSettingsService applicationSettingsService;

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

		boolean allowed = true;
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

	@GET
	@ApiOperation(value = "Returns information about API parameters", responseClass = "com.wordnik.swagger.core.Documentation")
	public Response getHelp(@Context Application app,
			@Context HttpHeaders headers, @Context UriInfo uriInfo) {

		TypeUtil.addAllowablePackage(Entries.class.getPackage().getName());
		String apiVersion = ApiListingResource.API_VERSION;
		String swaggerVersion = SwaggerSpec.version();
		String basePath = ApiListingResource
				.getBasePath(applicationSettingsService.get().getPublicUrl());

		Class<?> resource = null;
		String path = prependSlash(uriInfo.getPath());
		for (Class<?> klass : app.getClasses()) {
			Api api = klass.getAnnotation(Api.class);
			if (api != null && api.value() != null
					&& StringUtils.equals(prependSlash(api.value()), path)) {
				resource = klass;
				break;
			}
		}

		if (resource == null) {
			return Response
					.status(Status.NOT_FOUND)
					.entity("Api annotation not found on class "
							+ getClass().getName()).build();
		}
		Api api = resource.getAnnotation(Api.class);
		String apiPath = api.value();
		String apiListingPath = api.value();

		Documentation doc = new HelpApi(null).filterDocs(JaxrsApiReader.read(
				resource, apiVersion, swaggerVersion, basePath, apiPath),
				headers, uriInfo, apiListingPath, apiPath);

		doc.setSwaggerVersion(swaggerVersion);
		doc.setBasePath(basePath);
		doc.setApiVersion(apiVersion);
		return Response.ok().entity(doc).build();
	}

	private String prependSlash(String path) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return path;
	}
}
