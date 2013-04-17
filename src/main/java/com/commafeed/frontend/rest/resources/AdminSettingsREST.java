package com.commafeed.frontend.rest.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.commafeed.backend.model.ApplicationSettings;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.frontend.SecurityCheck;
import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@SecurityCheck(Role.ADMIN)
@Path("/admin/settings")
@Api(value = "/admin/settings", description = "Operations about application settings administration")
public class AdminSettingsREST {

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Path("/get")
	@GET
	@ApiOperation(value = "Retrieve application settings", notes = "Retrieve application settings", responseClass = "com.commafeed.backend.model.ApplicationSettings")
	public ApplicationSettings get() {
		return applicationSettingsService.get();
	}

	@Path("/save")
	@POST
	@ApiOperation(value = "Save application settings", notes = "Save application settings")
	public void save(@ApiParam(required = true) ApplicationSettings settings) {
		Preconditions.checkNotNull(settings);
		applicationSettingsService.save(settings);
	}
}
