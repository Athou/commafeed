package com.commafeed.frontend.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.http.HttpHeaders;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

@Provider
public class JsonProvider extends JacksonJsonProvider {

	private static final String CONTENT_TYPE_VALUE_SUFFIX = ";charset=UTF-8";
	private static final String CACHE_CONTROL_VALUE = "no-cache";

	@Override
	public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {

		httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, mediaType.toString() + CONTENT_TYPE_VALUE_SUFFIX);
		httpHeaders.putSingle(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_VALUE);
		httpHeaders.putSingle(HttpHeaders.PRAGMA, CACHE_CONTROL_VALUE);

		super.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, entityStream);
	}

}
