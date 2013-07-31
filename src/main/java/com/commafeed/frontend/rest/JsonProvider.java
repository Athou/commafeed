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

	private static final String HEADER_CORS = "Access-Control-Allow-Origin";
	private static final String HEADER_CORS_VALUE = "*";
	private static final String HEADER_CORS_METHODS = "Access-Control-Allow-Methods";
	private static final String HEADER_CORS_METHODS_VALUE = "POST, GET, OPTIONS";
	private static final String HEADER_CORS_MAXAGE = "Access-Control-Max-Age";
	private static final String HEADER_CORS_MAXAGE_VALUE = "2592000";
	private static final String HEADER_CORS_ALLOW_HEADERS = "Access-Control-Allow-Headers";
	private static final String HEADER_CORS_ALLOW_HEADERS_VALUE = "Authorization";

	private static final String HEADER_X_UA_COMPATIBLE = "X-UA-Compatible";
	private static final String HEADER_X_UA_COMPATIBLE_VALUE = "IE=Edge,chrome=1";

	@Override
	public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {

		httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, mediaType.toString() + CONTENT_TYPE_VALUE_SUFFIX);
		httpHeaders.putSingle(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_VALUE);
		httpHeaders.putSingle(HttpHeaders.PRAGMA, CACHE_CONTROL_VALUE);

		httpHeaders.putSingle(HEADER_CORS, HEADER_CORS_VALUE);
		httpHeaders.putSingle(HEADER_CORS_METHODS, HEADER_CORS_METHODS_VALUE);
		httpHeaders.putSingle(HEADER_CORS_MAXAGE, HEADER_CORS_MAXAGE_VALUE);
		httpHeaders.putSingle(HEADER_CORS_ALLOW_HEADERS, HEADER_CORS_ALLOW_HEADERS_VALUE);

		httpHeaders.putSingle(HEADER_X_UA_COMPATIBLE, HEADER_X_UA_COMPATIBLE_VALUE);

		super.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, entityStream);
	}

}
