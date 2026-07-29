package com.commafeed.frontend.model.request;

import lombok.Data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

@SuppressWarnings("serial")
@Schema(description = "Mark Request")
@Data
public class CollapseRequest implements Serializable {

    @Schema(description = "category id", required = true)
    private Long id;

    @Schema(description = "collapse", required = true)
    private boolean collapse;
}
