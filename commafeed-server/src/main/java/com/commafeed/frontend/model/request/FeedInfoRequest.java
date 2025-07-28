package com.commafeed.frontend.model.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Feed information request")
@Data
public class FeedInfoRequest implements Serializable {

	@Schema(description = "feed url", required = true)
	@NotEmpty
	@Size(max = 4096)
	private String url;

}
