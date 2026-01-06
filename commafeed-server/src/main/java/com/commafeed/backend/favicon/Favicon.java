package com.commafeed.backend.favicon;

import jakarta.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record Favicon(byte[] icon, MediaType mediaType) {

	private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.valueOf("image/x-icon");

	public Favicon(byte[] icon, String contentType) {
		this(icon, parseMediaType(contentType));
	}

	private static MediaType parseMediaType(String contentType) {
		try {
			return MediaType.valueOf(contentType);
		} catch (Exception e) {
			log.debug("invalid content type '{}' received, returning default value", contentType, e);
			return DEFAULT_MEDIA_TYPE;
		}
	}
}