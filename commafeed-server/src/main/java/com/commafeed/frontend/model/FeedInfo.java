package com.commafeed.frontend.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import lombok.Data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

@SuppressWarnings("serial")
@Schema(description = "Feed details")
@Data
@RegisterForReflection
public class FeedInfo implements Serializable {

    @Schema(description = "url", required = true)
    private String url;

    @Schema(description = "title", required = true)
    private String title;
}
