package com.commafeed.frontend.model.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Subscription request")
@Data
public class SubscribeRequest implements Serializable {

	@Schema(description = "url of the feed", required = true)
	@NotEmpty
	@Size(max = 4096)
	private String url;

	@Schema(description = "name of the feed for the user", required = true)
	@NotEmpty
	@Size(max = 128)
	private String title;

	@Schema(description = "id of the user category to place the feed in")
	@Size(max = 128)
	private String categoryId;
}
