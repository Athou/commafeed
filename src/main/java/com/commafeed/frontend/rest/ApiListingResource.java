package com.commafeed.frontend.rest;

import java.util.Enumeration;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.frontend.model.Entries;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.jaxrs.ConfigReader;
import com.wordnik.swagger.jaxrs.JavaApiListing;

@Path("/resources")
@Api("/resources")
@Produces({ "application/json" })
public class ApiListingResource extends JavaApiListing {

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Override
	@GET
	@ApiOperation(value = "Returns list of all available api endpoints", responseClass = "List[DocumentationEndPoint]")
	public Response getAllApis(@Context final ServletConfig sc,
			@Context Application app, @Context HttpHeaders headers,
			@Context UriInfo uriInfo) {

		return super.getAllApis(new ServletConfigProxy(
				applicationSettingsService.get().getPublicUrl(), sc), app,
				headers, uriInfo);
	}

	public static class ServletConfigProxy implements ServletConfig {

		private ServletConfig sc;
		private String publicUrl;

		public ServletConfigProxy(String publicUrl, ServletConfig sc) {
			this.sc = sc;
			this.publicUrl = publicUrl;
		}

		@Override
		public String getServletName() {
			return sc.getServletName();
		}

		@Override
		public ServletContext getServletContext() {
			return sc.getServletContext();
		}

		@Override
		public String getInitParameter(String name) {
			if ("swagger.config.reader".equals(name)) {
				return CustomConfigReader.class.getName();
			}
			if (CustomConfigReader.class.getName().equals(name)) {
				return publicUrl;
			}
			return sc.getInitParameter(name);
		}

		@Override
		public Enumeration<String> getInitParameterNames() {
			return sc.getInitParameterNames();
		}
	}

	public static class CustomConfigReader extends ConfigReader {

		private ServletConfig config;

		public CustomConfigReader(ServletConfig config) {
			this.config = config;
		}

		@Override
		public String basePath() {
			String publicUrl = config.getInitParameter(CustomConfigReader.class
					.getName());
			if (publicUrl.endsWith("/")) {
				publicUrl = publicUrl.substring(0, publicUrl.length() - 1);
			}
			return publicUrl + "/rest";
		}

		@Override
		public String swaggerVersion() {
			return SwaggerSpec.version();
		}

		@Override
		public String apiVersion() {
			return "1.0";
		}

		@Override
		public String modelPackages() {
			return Entries.class.getPackage().getName();
		}

		@Override
		public String apiFilterClassName() {
			return null;
		}
	}
}