package com.commafeed.frontend.rest.resources;

import java.util.Arrays;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.StartupBean;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.model.UserSettings.ReadingMode;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.backend.model.UserSettings.ViewMode;
import com.commafeed.frontend.SecurityCheck;
import com.commafeed.frontend.model.Settings;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.ProfileModificationRequest;
import com.commafeed.frontend.model.request.RegistrationRequest;
import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/user")
@Api(value = "/user", description = "Operations about the user")
public class UserREST extends AbstractResourceREST {

	@Path("/settings")
	@GET
	@ApiOperation(value = "Retrieve user settings", notes = "Retrieve user settings", responseClass = "com.commafeed.frontend.model.Settings")
	public Response getSettings() {
		Settings s = new Settings();
		UserSettings settings = userSettingsDAO.findByUser(getUser());
		if (settings != null) {
			// force unread for the moment
			// s.setReadingMode(settings.getReadingMode().name());
			s.setReadingMode(ReadingMode.unread.name());
			
			s.setReadingOrder(settings.getReadingOrder().name());
			s.setViewMode(settings.getViewMode().name());
			s.setShowRead(settings.isShowRead());
			s.setSocialButtons(settings.isSocialButtons());
			s.setScrollMarks(settings.isScrollMarks());
			s.setTheme(settings.getTheme());
			s.setCustomCss(settings.getCustomCss());
			s.setLanguage(settings.getLanguage());
		} else {
			s.setReadingMode(ReadingMode.unread.name());
			s.setReadingOrder(ReadingOrder.desc.name());
			s.setViewMode(ViewMode.title.name());
			s.setShowRead(true);
			s.setTheme("default");
			s.setSocialButtons(true);
			s.setScrollMarks(true);
			s.setLanguage("en");
		}
		return Response.ok(s).build();
	}

	@Path("/settings")
	@POST
	@ApiOperation(value = "Save user settings", notes = "Save user settings")
	public Response saveSettings(@ApiParam Settings settings) {
		Preconditions.checkNotNull(settings);

		if (startupBean.getSupportedLanguages().get(settings.getLanguage()) == null) {
			settings.setLanguage("en");
		}

		UserSettings s = userSettingsDAO.findByUser(getUser());
		if (s == null) {
			s = new UserSettings();
			s.setUser(getUser());
		}
		s.setReadingMode(ReadingMode.valueOf(settings.getReadingMode()));
		s.setReadingOrder(ReadingOrder.valueOf(settings.getReadingOrder()));
		s.setShowRead(settings.isShowRead());
		s.setViewMode(ViewMode.valueOf(settings.getViewMode()));
		s.setScrollMarks(settings.isScrollMarks());
		s.setTheme(settings.getTheme());
		s.setCustomCss(settings.getCustomCss());
		s.setSocialButtons(settings.isSocialButtons());
		s.setLanguage(settings.getLanguage());
		userSettingsDAO.saveOrUpdate(s);
		return Response.ok(Status.OK).build();

	}

	@Path("/profile")
	@GET
	@ApiOperation(value = "Retrieve user's profile", responseClass = "com.commafeed.frontend.model.UserModel")
	public Response get() {
		User user = getUser();
		UserModel userModel = new UserModel();
		userModel.setId(user.getId());
		userModel.setName(user.getName());
		userModel.setEmail(user.getEmail());
		userModel.setEnabled(!user.isDisabled());
		userModel.setApiKey(user.getApiKey());
		for (UserRole role : userRoleDAO.findAll(user)) {
			if (role.getRole() == Role.ADMIN) {
				userModel.setAdmin(true);
			}
		}
		return Response.ok(userModel).build();
	}

	@Path("/profile")
	@POST
	@ApiOperation(value = "Save user's profile")
	public Response save(
			@ApiParam(required = true) ProfileModificationRequest request) {
		User user = getUser();

		Preconditions.checkArgument(StringUtils.isBlank(request.getPassword())
				|| request.getPassword().length() >= 6);
		if (StringUtils.isNotBlank(request.getEmail())) {
			User u = userDAO.findByEmail(request.getEmail());
			Preconditions.checkArgument(u == null
					|| user.getId().equals(u.getId()));
		}

		if (StartupBean.USERNAME_DEMO.equals(user.getName())) {
			return Response.status(Status.FORBIDDEN).build();
		}

		user.setEmail(StringUtils.trimToNull(request.getEmail()));
		if (StringUtils.isNotBlank(request.getPassword())) {
			byte[] password = encryptionService.getEncryptedPassword(
					request.getPassword(), user.getSalt());
			user.setPassword(password);
			user.setApiKey(userService.generateApiKey(user));
		}
		if (request.isNewApiKey()) {
			user.setApiKey(userService.generateApiKey(user));
		}
		userDAO.saveOrUpdate(user);
		return Response.ok().build();
	}

	@Path("/register")
	@POST
	@ApiOperation(value = "Register a new account")
	@SecurityCheck(Role.NONE)
	public Response register(@ApiParam(required = true) RegistrationRequest req) {
		try {
			userService.register(req.getName(), req.getPassword(),
					req.getEmail(), Arrays.asList(Role.USER));
			return Response.ok().build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		}

	}

	@Path("/profile/deleteAccount")
	@POST
	@ApiOperation(value = "Delete the user account")
	public Response delete() {
		if (StartupBean.USERNAME_ADMIN.equals(getUser().getName())
				|| StartupBean.USERNAME_DEMO.equals(getUser().getName())) {
			return Response.status(Status.FORBIDDEN).build();
		}
		userService.unregister(getUser());
		return Response.ok().build();
	}
}
