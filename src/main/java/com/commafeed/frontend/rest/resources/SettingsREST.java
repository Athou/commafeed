package com.commafeed.frontend.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.model.UserSettings.ReadingMode;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.frontend.model.Settings;
import com.google.common.base.Preconditions;

@Path("settings")
public class SettingsREST extends AbstractREST {

	@Path("get")
	@GET
	public Settings get() {
		Settings s = new Settings();
		UserSettings settings = userSettingsService.findByUser(getUser());
		if (settings != null) {
			s.setReadingMode(settings.getReadingMode().name());
			s.setReadingOrder(settings.getReadingOrder().name());
			s.setCustomCss(settings.getCustomCss());
		} else {
			s.setReadingMode(ReadingMode.unread.name());
			s.setReadingOrder(ReadingOrder.desc.name());
		}
		return s;
	}

	@Path("save")
	@POST
	public Response save(Settings settings) {
		Preconditions.checkNotNull(settings);

		UserSettings s = userSettingsService.findByUser(getUser());
		if (s == null) {
			s = new UserSettings();
			s.setUser(getUser());
		}
		s.setReadingMode(ReadingMode.valueOf(settings.getReadingMode()));
		s.setReadingOrder(ReadingOrder.valueOf(settings.getReadingOrder()));
		s.setCustomCss(settings.getCustomCss());
		userSettingsService.saveOrUpdate(s);
		return Response.ok(Status.OK).build();

	}
}
