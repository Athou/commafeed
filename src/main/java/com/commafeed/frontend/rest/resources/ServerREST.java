package com.commafeed.frontend.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.commafeed.frontend.model.ServerInfo;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/server")
@Api(value = "/server", description = "Operations about server infos")
public class ServerREST extends AbstractResourceREST {

	@Path("/get")
	@GET
	@ApiOperation(value = "Get server infos", notes = "Get server infos", responseClass = "com.commafeed.frontend.model.ServerInfo")
	public Response get() {
		ServerInfo infos = new ServerInfo();
		infos.setAnnouncement(applicationSettingsService.get()
				.getAnnouncement());
		infos.getSupportedLanguages().putAll(
				startupBean.getSupportedLanguages());
		return Response.ok(infos).build();
	}
}
