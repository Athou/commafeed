package com.commafeed.frontend.rest.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.request.MarkRequest;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.core.util.TypeUtil;

@Path("/resources")
@Api("/resources")
public class ApiDocumentationREST extends AbstractREST {

	public static final String API_VERSION = "1.0";

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@GET
	@ApiOperation(value = "Returns list of all available api endpoints", responseClass = "List[DocumentationEndPoint]")
	public Response getAllApis(@Context Application app) {

		TypeUtil.addAllowablePackage(Entries.class.getPackage().getName());
		TypeUtil.addAllowablePackage(MarkRequest.class.getPackage().getName());

		Documentation doc = new Documentation();
		for (Class<?> resource : app.getClasses()) {
			if (ApiDocumentationREST.class.equals(resource)) {
				continue;
			}
			Api api = resource.getAnnotation(Api.class);
			if (api != null) {
				doc.addApi(new DocumentationEndPoint(api.value(), api
						.description()));
			}
		}

		doc.setSwaggerVersion(SwaggerSpec.version());
		doc.setBasePath(getBasePath(applicationSettingsService.get()
				.getPublicUrl()));
		doc.setApiVersion(API_VERSION);

		return Response.ok().entity(doc).build();
	}

	public static String getBasePath(String publicUrl) {
		if (!publicUrl.endsWith("/")) {
			publicUrl = publicUrl + "/";
		}
		publicUrl += "rest";

		return publicUrl;
	}

}