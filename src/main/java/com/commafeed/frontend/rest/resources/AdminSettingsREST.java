package com.commafeed.frontend.rest.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.commafeed.backend.dao.ApplicationSettingsDAO;
import com.commafeed.backend.model.ApplicationSettings;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.frontend.SecurityCheck;

@SecurityCheck(Role.ADMIN)
@Path("admin/settings")
public class AdminSettingsREST {

	@Inject
	ApplicationSettingsDAO applicationSettingsDAO;

	@Path("get")
	@GET
	public ApplicationSettings get() {
		return applicationSettingsDAO.get();
	}

	@Path("save")
	@POST
	public Response save(ApplicationSettings settings) {
		applicationSettingsDAO.save(settings);
		return Response.ok().build();
	}
}
