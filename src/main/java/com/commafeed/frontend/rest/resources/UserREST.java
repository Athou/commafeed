package com.commafeed.frontend.rest.resources;

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
import com.commafeed.frontend.model.Settings;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.ProfileModificationRequest;
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
	public Settings getSettings() {
		Settings s = new Settings();
		UserSettings settings = userSettingsDAO.findByUser(getUser());
		if (settings != null) {
			s.setReadingMode(settings.getReadingMode().name());
			s.setReadingOrder(settings.getReadingOrder().name());
			s.setShowRead(settings.isShowRead());
			s.setCustomCss(settings.getCustomCss());
		} else {
			s.setReadingMode(ReadingMode.unread.name());
			s.setReadingOrder(ReadingOrder.desc.name());
			s.setShowRead(true);
		}
		return s;
	}

	@Path("/settings")
	@POST
	@ApiOperation(value = "Save user settings", notes = "Save user settings")
	public Response saveSettings(@ApiParam Settings settings) {
		Preconditions.checkNotNull(settings);

		UserSettings s = userSettingsDAO.findByUser(getUser());
		if (s == null) {
			s = new UserSettings();
			s.setUser(getUser());
		}
		s.setReadingMode(ReadingMode.valueOf(settings.getReadingMode()));
		s.setReadingOrder(ReadingOrder.valueOf(settings.getReadingOrder()));
		s.setShowRead(settings.isShowRead());
		s.setCustomCss(settings.getCustomCss());
		userSettingsDAO.saveOrUpdate(s);
		return Response.ok(Status.OK).build();

	}

	@Path("/profile")
	@GET
	@ApiOperation(value = "Retrieve user's profile", responseClass = "com.commafeed.frontend.model.UserModel")
	public UserModel get() {
		User user = getUser();
		UserModel userModel = new UserModel();
		userModel.setId(user.getId());
		userModel.setName(user.getName());
		userModel.setEmail(user.getEmail());
		userModel.setEnabled(!user.isDisabled());
		for (UserRole role : userRoleDAO.findAll(user)) {
			if (role.getRole() == Role.ADMIN) {
				userModel.setAdmin(true);
			}
		}
		return userModel;
	}

	@Path("/profile")
	@POST
	@ApiOperation(value = "Save user's profile")
	public Response save(
			@ApiParam(required = true) ProfileModificationRequest request) {
		User user = getUser();
		if (StartupBean.USERNAME_DEMO.equals(user.getName())) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		user.setEmail(request.getEmail());
		if (StringUtils.isNotBlank(request.getPassword())) {
			byte[] password = encryptionService.getEncryptedPassword(
					request.getPassword(), user.getSalt());
			user.setPassword(password);
		}
		userDAO.update(user);
		return Response.ok().build();
	}
}
