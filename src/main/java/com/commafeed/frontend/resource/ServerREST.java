package com.commafeed.frontend.resource;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import com.codahale.metrics.annotation.Timed;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.auth.SecurityCheck;
import com.commafeed.frontend.model.ServerInfo;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

@Path("/server")
@Api(value = "/server")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor = @__({ @Inject }) )
@Singleton
public class ServerREST {

	private final HttpGetter httpGetter;
	private final CommaFeedConfiguration config;

	@Path("/get")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Get server infos", notes = "Get server infos", response = ServerInfo.class)
	@Timed
	public Response get() {
		ServerInfo infos = new ServerInfo();
		infos.setAnnouncement(config.getApplicationSettings().getAnnouncement());
		infos.setVersion(config.getVersion());
		infos.setGitCommit(config.getGitCommit());
		infos.setAllowRegistrations(config.getApplicationSettings().getAllowRegistrations());
		infos.setGoogleAnalyticsCode(config.getApplicationSettings().getGoogleAnalyticsTrackingCode());
		infos.setSmtpEnabled(StringUtils.isNotBlank(config.getApplicationSettings().getSmtpHost()));
		return Response.ok(infos).build();
	}

	@Path("/proxy")
	@GET
	@UnitOfWork
	@ApiOperation(value = "proxy image")
	@Produces("image/png")
	@Timed
	public Response get(@SecurityCheck User user, @QueryParam("u") String url) {
		if (!config.getApplicationSettings().getImageProxyEnabled()) {
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
