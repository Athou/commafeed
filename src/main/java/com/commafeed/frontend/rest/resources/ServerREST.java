package com.commafeed.frontend.rest.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.StartupBean;
import com.commafeed.backend.feeds.FeedUtils;
import com.commafeed.backend.services.ApplicationPropertiesService;
import com.commafeed.frontend.model.ServerInfo;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/server")
@Api(value = "/server", description = "Operations about server infos")
public class ServerREST extends AbstractResourceREST {

	@Inject
	StartupBean startupBean;

	@Inject
	HttpGetter httpGetter;

	@Path("/get")
	@GET
	@ApiOperation(value = "Get server infos", notes = "Get server infos", responseClass = "com.commafeed.frontend.model.ServerInfo")
	public Response get() {
		ApplicationPropertiesService properties = ApplicationPropertiesService.get();

		ServerInfo infos = new ServerInfo();
		infos.setAnnouncement(applicationSettingsService.get()
				.getAnnouncement());
		infos.getSupportedLanguages().putAll(
				startupBean.getSupportedLanguages());
		infos.setVersion(properties.getVersion());
		infos.setGitCommit(properties.getGitCommit());
		return Response.ok(infos).build();
	}

	@Path("/proxy")
	@GET
	@ApiOperation(value = "proxy image")
	@Produces("image/png")
	public Response get(@QueryParam("u") String url) {
		if (!applicationSettingsService.get().isImageProxyEnabled()) {
			return Response.status(Status.FORBIDDEN).build();
		}

		url = FeedUtils.imageProxyDecoder(url);
		try {
			HttpResult result = httpGetter.getBinary(url, 20000);
			return Response.ok(result.getContent()).build();
		} catch (Exception e) {
			return Response.status(Status.SERVICE_UNAVAILABLE)
					.entity(e.getMessage()).build();
		}
	}
}
