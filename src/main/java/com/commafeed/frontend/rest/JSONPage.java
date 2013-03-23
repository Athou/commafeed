package com.commafeed.frontend.rest;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.commafeed.frontend.CommaFeedSession;
import com.commafeed.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

@SuppressWarnings("serial")
public abstract class JSONPage extends WebPage {

	public JSONPage(PageParameters pageParameters) {
		Gson gson = new GsonBuilder().registerTypeAdapter(Date.class,
				new DateSerializer()).create();
		getRequestCycle().scheduleRequestHandlerAfterCurrent(
				new TextRequestHandler("application/json", "UTF-8", gson
						.toJson(getObject(pageParameters))));
	}

	protected abstract Object getObject(PageParameters parameters);

	protected User getUser() {
		return CommaFeedSession.get().getUser();
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
