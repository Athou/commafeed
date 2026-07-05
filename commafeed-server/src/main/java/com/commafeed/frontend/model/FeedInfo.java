package com.commafeed.frontend.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

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
