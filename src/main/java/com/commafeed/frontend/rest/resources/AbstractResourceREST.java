package com.commafeed.frontend.rest.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.frontend.SecurityCheck;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.request.MarkRequest;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.core.util.TypeUtil;
import com.wordnik.swagger.jaxrs.HelpApi;
import com.wordnik.swagger.jaxrs.JaxrsApiReader;

@SecurityCheck(Role.USER)
public abstract class AbstractResourceREST extends AbstractREST {

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@GET
	@SecurityCheck(value = Role.NONE)
	@ApiOperation(value = "Returns information about API parameters", responseClass = "com.wordnik.swagger.core.Documentation")
	public Response getHelp(@Context Application app,
			@Context HttpHeaders headers, @Context UriInfo uriInfo) {

		TypeUtil.addAllowablePackage(Entries.class.getPackage().getName());
		TypeUtil.addAllowablePackage(MarkRequest.class.getPackage().getName());

		String apiVersion = ApiDocumentationREST.API_VERSION;
		String swaggerVersion = SwaggerSpec.version();
		String basePath = ApiDocumentationREST
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
