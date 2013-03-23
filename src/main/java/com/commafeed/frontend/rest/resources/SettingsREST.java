package com.commafeed.frontend.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.model.UserSettings.ReadingMode;
import com.commafeed.frontend.model.Settings;

@Path("settings")
public class SettingsREST extends AbstractREST {

	@Path("get")
	@GET
	public Settings get() {
		UserSettings settings = userSettingsService.findByUser(getUser());
		Settings s = new Settings();
		s.setReadingMode(settings.getReadingMode().name().toLowerCase());
		return s;
	}

	@Path("save")
	@POST
	public void save(Settings settings) {
		UserSettings s = userSettingsService.findByUser(getUser());
		if (s == null) {
			s = new UserSettings();
			s.setUser(getUser());
		}
		s.setReadingMode(ReadingMode.valueOf(settings.getReadingMode()));
		userSettingsService.saveOrUpdate(s);

	}
}
