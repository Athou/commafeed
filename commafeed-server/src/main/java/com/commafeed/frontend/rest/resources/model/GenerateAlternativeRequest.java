package com.commafeed.frontend.rest.resources.model;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

@SuppressWarnings("serial")
@Schema(description = "Generate alternative request")
@Data
public class GenerateAlternativeRequest implements Serializable {

    @Schema(description = "entry field to rewrite: title or content", required = true)
    @NotBlank
    private String target;

    @Schema(description = "instructions for the generated alternative", required = true)
    @NotBlank
    private String prompt;
}
