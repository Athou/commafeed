package com.commafeed.frontend.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.Data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

@SuppressWarnings("serial")
@Schema(description = "Feed information request")
@Data
public class FeedInfoRequest implements Serializable {

    @Schema(description = "feed url", required = true)
    @NotEmpty
    @Size(max = 4096)
    private String url;
}
