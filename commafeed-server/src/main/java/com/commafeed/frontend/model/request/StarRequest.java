package com.commafeed.frontend.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.Data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

@SuppressWarnings("serial")
@Schema(description = "Star Request")
@Data
public class StarRequest implements Serializable {

    @Schema(description = "id", required = true)
    @NotEmpty
    @Size(max = 128)
    private String id;

    @Schema(description = "starred or not", required = true)
    private boolean starred;
}
