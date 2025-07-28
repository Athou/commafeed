package com.commafeed.frontend.model;

import java.io.Serializable;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

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
