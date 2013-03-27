package com.commafeed.frontend.rest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.time.DateUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JSONMessageBodyReaderWriter implements MessageBodyWriter<Object>,
		MessageBodyReader<Object> {

	private static final String UTF_8 = "UTF-8";

	private Gson gson;

	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return true;
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return true;
	}

	@Override
	public long getSize(Object t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(Object t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, mediaType.toString()
				+ ";charset=UTF-8");
		OutputStreamWriter writer = new OutputStreamWriter(entityStream, UTF_8);
		getGson().toJson(t, type, writer);
		writer.flush();
	}

	@Override
	public Object readFrom(Class<Object> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		InputStreamReader reader = new InputStreamReader(
				new BufferedInputStream(entityStream), UTF_8);
		return getGson().fromJson(reader, type);
	}

	private Gson getGson() {
		if (gson == null) {
			gson = new GsonBuilder().registerTypeAdapter(Date.class,
					new DateSerializer()).create();
		}
		return gson;
	}

	private static class DateSerializer implements JsonSerializer<Date> {

		private static final String DAY_FORMAT = "yyyy-MM-dd";
		private static final String TIME_FORMAT = "HH:mm";

		public JsonElement serialize(Date src, Type typeOfSrc,
				JsonSerializationContext context) {
			Date now = Calendar.getInstance().getTime();
			String format = DateUtils.isSameDay(now, src) ? TIME_FORMAT
					: DAY_FORMAT;
			return new JsonPrimitive(new SimpleDateFormat(format).format(src));
		}
	}

}
