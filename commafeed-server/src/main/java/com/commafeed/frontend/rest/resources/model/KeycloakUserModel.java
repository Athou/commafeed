package com.commafeed.frontend.rest.resources.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

@SuppressWarnings("serial")
@Schema(description = "Keycloak user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class KeycloakUserModel implements Serializable {

    @Schema(description = "Keycloak user id", required = true)
    private String id;

    @Schema(description = "username", required = true)
    private String username;

    @Schema(description = "email")
    private String email;

    @Schema(description = "first name")
    private String firstName;

    @Schema(description = "last name")
    private String lastName;

    @Schema(description = "whether the user is enabled", required = true)
    private boolean enabled;

    @Schema(description = "creation timestamp")
    private Long createdTimestamp;
}
