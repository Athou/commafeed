package com.commafeed.frontend.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@SuppressWarnings("serial")
@Data
@Schema
public class PasswordResetRequest implements Serializable {

    @Schema(description = "email address for password recovery", required = true)
    @Email
    @NotEmpty
    @Size(max = 255)
    private String email;
}
