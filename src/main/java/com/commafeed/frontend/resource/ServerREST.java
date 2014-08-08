package com.commafeed.frontend.resource;

import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.AllArgsConstructor;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.ApplicationPropertiesService;
import com.commafeed.frontend.auth.SecurityCheck;
import com.commafeed.frontend.model.ServerInfo;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/server")
@Api(value = "/server", description = "Operations about server infos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@AllArgsConstructor
public class ServerREST {

	private final HttpGetter httpGetter;
	private final CommaFeedConfiguration config;
	private final ApplicationPropertiesService applicationPropertiesService;

	@Path("/get")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Get server infos", notes = "Get server infos", response = ServerInfo.class)
	public Response get(@SecurityCheck User user) {
		ServerInfo infos = new ServerInfo();
		infos.setAnnouncement(config.getApplicationSettings().getAnnouncement());
		infos.setVersion(applicationPropertiesService.getVersion());
		infos.setGitCommit(applicationPropertiesService.getGitCommit());
		return Response.ok(infos).build();
	}

	@Path("/proxy")
	@GET
	@UnitOfWork
	@ApiOperation(value = "proxy image")
	@Produces("image/png")
	public Response get(@SecurityCheck User user, @QueryParam("u") String url) {
		if (!config.getApplicationSettings().isImageProxyEnabled()) {
			return Response.status(Status.FORBIDDEN).build();
		}

		url = FeedUtils.imageProxyDecoder(url);
		try {
			HttpResult result = httpGetter.getBinary(url, 20000);
			return Response.ok(result.getContent()).build();
		} catch (Exception e) {
			return Response.status(Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build();
		}
	}
}
