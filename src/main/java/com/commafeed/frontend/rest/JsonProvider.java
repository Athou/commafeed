package com.commafeed.frontend.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.http.HttpHeaders;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

	private static final String CONTENT_TYPE_VALUE_SUFFIX = ";charset=UTF-8";
	private static final String CACHE_CONTROL_VALUE = "no-cache";

	private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Context
	private HttpServletRequest request;

	@Override
	public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {

		httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, mediaType.toString() + CONTENT_TYPE_VALUE_SUFFIX);
		httpHeaders.putSingle(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_VALUE);
		httpHeaders.putSingle(HttpHeaders.PRAGMA, CACHE_CONTROL_VALUE);

		ObjectWriter writer = getMapper().writer();
		if (hasPrettyPrint(annotations)) {
			writer = writer.withDefaultPrettyPrinter();
		}
		writer.writeValue(entityStream, value);

	}

	private boolean hasPrettyPrint(Annotation[] annotations) {
		boolean prettyPrint = false;

		for (Annotation annotation : annotations) {
			if (PrettyPrint.class.equals(annotation.annotationType())) {
				prettyPrint = true;
				break;
			}
		}
		if (!prettyPrint && request != null) {
			prettyPrint = Boolean.parseBoolean(request.getParameter("pretty"));
		}
		return prettyPrint;
	}

	@Override
	public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
		return getMapper().readValue(entityStream, type);
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return true;
	}

	@Override
	public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return true;
	}

	public static ObjectMapper getMapper() {
		return MAPPER;
	}

}
