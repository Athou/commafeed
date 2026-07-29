package com.commafeed.frontend.model.request;

import lombok.Data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

@SuppressWarnings("serial")
@Schema
@Data
public class IDRequest implements Serializable {

    @Schema(required = true)
    private Long id;
}
