package com.commafeed.frontend.rest.resources;

import com.commafeed.backend.service.KeycloakAdminService;
import com.commafeed.frontend.rest.resources.model.KeycloakUserModel;
import com.commafeed.security.Roles;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/rest/admin/keycloak")
@RolesAllowed(Roles.ADMIN)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Singleton
@Tag(name = "Keycloak administration")
public class KeycloakUserREST {

    private final KeycloakAdminService keycloakAdminService;

    @Path("/users")
    @GET
    @Operation(summary = "List Keycloak users")
    public Response getUsers() {
        try {
            List<KeycloakUserModel> users = keycloakAdminService.findUsers();
            return Response.ok(users).build();
        } catch (KeycloakAdminService.KeycloakUnavailableException e) {
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }
    }

    @Path("/users/{id}")
    @GET
    @Operation(summary = "Get a Keycloak user")
    public Response getUser(
            @Parameter(description = "Keycloak user id", required = true)
                    @PathParam("id")
                    String id) {
        try {
            return Response.ok(keycloakAdminService.findUser(id)).build();
        } catch (KeycloakAdminService.KeycloakUserNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        } catch (KeycloakAdminService.KeycloakUnavailableException e) {
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }
    }
}
