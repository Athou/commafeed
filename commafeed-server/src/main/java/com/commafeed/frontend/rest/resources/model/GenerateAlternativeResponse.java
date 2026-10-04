package com.commafeed.frontend.rest.resources.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

@SuppressWarnings("serial")
@Schema(description = "Generated entry alternative")
@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class GenerateAlternativeResponse implements Serializable {

    @Schema(description = "original entry field value", required = true)
    private String originalEntry;

    @Schema(description = "entry field rewritten", required = true)
    private String target;

    @Schema(description = "instructions used to generate the alternative", required = true)
    private String prompt;

    @Schema(description = "generated alternative", required = true)
    private String generatedAlternative;
}
