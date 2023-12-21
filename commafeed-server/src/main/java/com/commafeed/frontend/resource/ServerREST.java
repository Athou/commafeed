package com.commafeed.frontend.resource;

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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;

@Path("/server")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class ServerREST {

	private final HttpGetter httpGetter;
	private final CommaFeedConfiguration config;

	@Path("/get")
	@GET
	@UnitOfWork
	@Operation(
			summary = "Get server infos",
			description = "Get server infos",
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = ServerInfo.class))) })
	@Timed
	public Response getServerInfos() {
		ServerInfo infos = new ServerInfo();
		infos.setAnnouncement(config.getApplicationSettings().getAnnouncement());
		infos.setVersion(config.getVersion());
		infos.setGitCommit(config.getGitCommit());
		infos.setAllowRegistrations(config.getApplicationSettings().getAllowRegistrations());
		infos.setGoogleAnalyticsCode(config.getApplicationSettings().getGoogleAnalyticsTrackingCode());
		infos.setSmtpEnabled(StringUtils.isNotBlank(config.getApplicationSettings().getSmtpHost()));
		infos.setDemoAccountEnabled(config.getApplicationSettings().getCreateDemoAccount());
		infos.setWebsocketEnabled(config.getApplicationSettings().getWebsocketEnabled());
		infos.setWebsocketPingInterval(config.getApplicationSettings().getWebsocketPingInterval().toMilliseconds());
		infos.setTreeReloadInterval(config.getApplicationSettings().getTreeReloadInterval().toMilliseconds());
		return Response.ok(infos).build();
	}

	@Path("/proxy")
	@GET
	@UnitOfWork
	@Operation(summary = "proxy image")
	@Produces("image/png")
	@Timed
	public Response getProxiedImage(@Parameter(hidden = true) @SecurityCheck User user,
			@Parameter(description = "image url", required = true) @QueryParam("u") String url) {
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
