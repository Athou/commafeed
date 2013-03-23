package com.commafeed.frontend.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JSONMessageBodyWriter implements MessageBodyWriter<Object> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
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

		Gson gson = new GsonBuilder().registerTypeAdapter(Date.class,
				new DateSerializer()).create();
		IOUtils.write(gson.toJson(t), entityStream, "UTF-8");

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
