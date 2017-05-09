package com.commafeed.frontend.resource;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.client.utils.URIBuilder;

import com.codahale.metrics.annotation.Timed;
import com.commafeed.CommaFeedApplication;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.model.UserSettings.ReadingMode;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.backend.model.UserSettings.ViewMode;
import com.commafeed.backend.service.MailService;
import com.commafeed.backend.service.PasswordEncryptionService;
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.auth.SecurityCheck;
import com.commafeed.frontend.model.Settings;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.LoginRequest;
import com.commafeed.frontend.model.request.PasswordResetRequest;
import com.commafeed.frontend.model.request.ProfileModificationRequest;
import com.commafeed.frontend.model.request.RegistrationRequest;
import com.commafeed.frontend.session.SessionHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.validation.ValidationErrorMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Path("/user")
@Api(value = "/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class UserREST {

	private final UserDAO userDAO;
	private final UserRoleDAO userRoleDAO;
	private final UserSettingsDAO userSettingsDAO;
	private final UserService userService;
	private final PasswordEncryptionService encryptionService;
	private final MailService mailService;
	private final CommaFeedConfiguration config;

	@Path("/settings")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Retrieve user settings", notes = "Retrieve user settings", response = Settings.class)
	@Timed
	public Response getSettings(@SecurityCheck User user) {
		Settings s = new Settings();
		UserSettings settings = userSettingsDAO.findByUser(user);
		if (settings != null) {
			s.setReadingMode(settings.getReadingMode().name());
			s.setReadingOrder(settings.getReadingOrder().name());
			s.setViewMode(settings.getViewMode().name());
			s.setShowRead(settings.isShowRead());

			s.setEmail(settings.isEmail());
			s.setGmail(settings.isGmail());
			s.setFacebook(settings.isFacebook());
			s.setTwitter(settings.isTwitter());
			s.setGoogleplus(settings.isGoogleplus());
			s.setTumblr(settings.isTumblr());
			s.setPocket(settings.isPocket());
			s.setInstapaper(settings.isInstapaper());
			s.setBuffer(settings.isBuffer());
			s.setReadability(settings.isReadability());

			s.setScrollMarks(settings.isScrollMarks());
			s.setTheme(settings.getTheme());
			s.setCustomCss(settings.getCustomCss());
			s.setLanguage(settings.getLanguage());
			s.setScrollSpeed(settings.getScrollSpeed());
		} else {
			s.setReadingMode(ReadingMode.unread.name());
			s.setReadingOrder(ReadingOrder.desc.name());
			s.setViewMode(ViewMode.title.name());
			s.setShowRead(true);
			s.setTheme("default");

			s.setEmail(true);
			s.setGmail(true);
			s.setFacebook(true);
			s.setTwitter(true);
			s.setGoogleplus(true);
			s.setTumblr(true);
			s.setPocket(true);
			s.setInstapaper(true);
			s.setBuffer(true);
			s.setReadability(true);

			s.setScrollMarks(true);
			s.setLanguage("en");
			s.setScrollSpeed(400);
		}
		return Response.ok(s).build();
	}

	@Path("/settings")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Save user settings", notes = "Save user settings")
	@Timed
	public Response saveSettings(@SecurityCheck User user, @ApiParam(required = true) Settings settings) {
		Preconditions.checkNotNull(settings);

		UserSettings s = userSettingsDAO.findByUser(user);
		if (s == null) {
			s = new UserSettings();
			s.setUser(user);
		}
		s.setReadingMode(ReadingMode.valueOf(settings.getReadingMode()));
		s.setReadingOrder(ReadingOrder.valueOf(settings.getReadingOrder()));
		s.setShowRead(settings.isShowRead());
		s.setViewMode(ViewMode.valueOf(settings.getViewMode()));
		s.setScrollMarks(settings.isScrollMarks());
		s.setTheme(settings.getTheme());
		s.setCustomCss(settings.getCustomCss());
		s.setLanguage(settings.getLanguage());
		s.setScrollSpeed(settings.getScrollSpeed());

		s.setEmail(settings.isEmail());
		s.setGmail(settings.isGmail());
		s.setFacebook(settings.isFacebook());
		s.setTwitter(settings.isTwitter());
		s.setGoogleplus(settings.isGoogleplus());
		s.setTumblr(settings.isTumblr());
		s.setPocket(settings.isPocket());
		s.setInstapaper(settings.isInstapaper());
		s.setBuffer(settings.isBuffer());
		s.setReadability(settings.isReadability());

		userSettingsDAO.saveOrUpdate(s);
		return Response.ok().build();

	}

	@Path("/profile")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Retrieve user's profile", response = UserModel.class)
	@Timed
	public Response get(@SecurityCheck User user) {
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
	@UnitOfWork
	@ApiOperation(value = "Save user's profile")
	@Timed
	public Response save(@SecurityCheck User user, @ApiParam(required = true) ProfileModificationRequest request) {
		Preconditions.checkArgument(StringUtils.isBlank(request.getPassword()) || request.getPassword().length() >= 6);
		if (StringUtils.isNotBlank(request.getEmail())) {
			User u = userDAO.findByEmail(request.getEmail());
			Preconditions.checkArgument(u == null || user.getId().equals(u.getId()));
		}

		if (CommaFeedApplication.USERNAME_DEMO.equals(user.getName())) {
			return Response.status(Status.FORBIDDEN).build();
		}

		user.setEmail(StringUtils.trimToNull(request.getEmail()));
		if (StringUtils.isNotBlank(request.getPassword())) {
			byte[] password = encryptionService.getEncryptedPassword(request.getPassword(), user.getSalt());
			user.setPassword(password);
			user.setApiKey(userService.generateApiKey(user));
		}
		if (request.isNewApiKey()) {
			user.setApiKey(userService.generateApiKey(user));
		}
		userDAO.update(user);
		return Response.ok().build();
	}

	@Path("/register")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Register a new account")
	@Timed
	public Response register(@Valid @ApiParam(required = true) RegistrationRequest req, @Context SessionHelper sessionHelper) {
		try {
			User registeredUser = userService.register(req.getName(), req.getPassword(), req.getEmail(), Arrays.asList(Role.USER));
			userService.login(req.getName(), req.getPassword());
			sessionHelper.setLoggedInUser(registeredUser);
			return Response.ok().build();
		} catch (final IllegalArgumentException e) {
			return Response.status(422).entity(new ValidationErrorMessage(ImmutableList.of(e.getMessage()))).type(MediaType.TEXT_PLAIN)
					.build();
		}
	}

	@Path("/login")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Login and create a session")
	@Timed
	public Response login(@ApiParam(required = true) LoginRequest req, @Context SessionHelper sessionHelper) {
		Optional<User> user = userService.login(req.getName(), req.getPassword());
		if (user.isPresent()) {
			sessionHelper.setLoggedInUser(user.get());
			return Response.ok().build();
		} else {
			return Response.status(Response.Status.UNAUTHORIZED).entity("wrong username or password").type(MediaType.TEXT_PLAIN).build();
		}
	}

	@Path("/passwordReset")
	@POST
	@UnitOfWork
	@ApiOperation(value = "send a password reset email")
	@Timed
	public Response sendPasswordReset(@Valid PasswordResetRequest req) {
		User user = userDAO.findByEmail(req.getEmail());
		if (user == null) {
			return Response.status(Status.PRECONDITION_FAILED).entity("Email not found.").type(MediaType.TEXT_PLAIN).build();
		}
		try {
			user.setRecoverPasswordToken(DigestUtils.sha1Hex(UUID.randomUUID().toString()));
			user.setRecoverPasswordTokenDate(new Date());
			userDAO.saveOrUpdate(user);
			mailService.sendMail(user, "Password recovery", buildEmailContent(user));
			return Response.ok().build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("could not send email: " + e.getMessage())
					.type(MediaType.TEXT_PLAIN).build();
		}
	}

	private String buildEmailContent(User user) throws Exception {
		String publicUrl = FeedUtils.removeTrailingSlash(config.getApplicationSettings().getPublicUrl());
		publicUrl += "/rest/user/passwordResetCallback";
		return String.format(
				"You asked for password recovery for account '%s', <a href='%s'>follow this link</a> to change your password. Ignore this if you didn't request a password recovery.",
				user.getName(), callbackUrl(user, publicUrl));
	}

	private String callbackUrl(User user, String publicUrl) throws Exception {
		return new URIBuilder(publicUrl).addParameter("email", user.getEmail()).addParameter("token", user.getRecoverPasswordToken())
				.build().toURL().toString();
	}

	@Path("/passwordResetCallback")
	@GET
	@UnitOfWork
	@Produces(MediaType.TEXT_HTML)
	@Timed
	public Response passwordRecoveryCallback(@QueryParam("email") String email, @QueryParam("token") String token) {
		Preconditions.checkNotNull(email);
		Preconditions.checkNotNull(token);

		User user = userDAO.findByEmail(email);
		if (user == null) {
			return Response.status(Status.UNAUTHORIZED).entity("Email not found.").build();
		}
		if (user.getRecoverPasswordToken() == null || !user.getRecoverPasswordToken().equals(token)) {
			return Response.status(Status.UNAUTHORIZED).entity("Invalid token.").build();
		}
		if (user.getRecoverPasswordTokenDate().before(DateUtils.addDays(new Date(), -2))) {
			return Response.status(Status.UNAUTHORIZED).entity("token expired.").build();
		}

		String passwd = RandomStringUtils.randomAlphanumeric(10);
		byte[] encryptedPassword = encryptionService.getEncryptedPassword(passwd, user.getSalt());
		user.setPassword(encryptedPassword);
		if (StringUtils.isNotBlank(user.getApiKey())) {
			user.setApiKey(userService.generateApiKey(user));
		}
		user.setRecoverPasswordToken(null);
		user.setRecoverPasswordTokenDate(null);
		userDAO.saveOrUpdate(user);

		String message = "Your new password is: " + passwd;
		message += "<br />";
		message += String.format("<a href=\"%s\">Back to Homepage</a>", config.getApplicationSettings().getPublicUrl());
		return Response.ok(message).build();
	}

	@Path("/profile/deleteAccount")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Delete the user account")
	@Timed
	public Response delete(@SecurityCheck User user) {
		if (CommaFeedApplication.USERNAME_ADMIN.equals(user.getName()) || CommaFeedApplication.USERNAME_DEMO.equals(user.getName())) {
			return Response.status(Status.FORBIDDEN).build();
		}
		userService.unregister(userDAO.findById(user.getId()));
		return Response.ok().build();
	}
}
