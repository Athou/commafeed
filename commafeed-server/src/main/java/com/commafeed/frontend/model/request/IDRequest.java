package com.commafeed.frontend.model.request;

import java.io.Serializable;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.Data;

@SuppressWarnings("serial")
@Schema
@Data
public class IDRequest implements Serializable {

	@Schema(required = true)
	private Long id;

}
