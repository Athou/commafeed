package com.commafeed.frontend.resource;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.ws.rs.BadRequestException;
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

import io.dropwizard.hibernate.UnitOfWork;
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
	public Response getUserSettings(@ApiParam(hidden = true) @SecurityCheck User user) {
		Settings s = new Settings();
		UserSettings settings = userSettingsDAO.findByUser(user);
		if (settings != null) {
			s.setReadingMode(settings.getReadingMode().name());
			s.setReadingOrder(settings.getReadingOrder().name());
			s.setShowRead(settings.isShowRead());

			s.getSharingSettings().setEmail(settings.isEmail());
			s.getSharingSettings().setGmail(settings.isGmail());
			s.getSharingSettings().setFacebook(settings.isFacebook());
			s.getSharingSettings().setTwitter(settings.isTwitter());
			s.getSharingSettings().setTumblr(settings.isTumblr());
			s.getSharingSettings().setPocket(settings.isPocket());
			s.getSharingSettings().setInstapaper(settings.isInstapaper());
			s.getSharingSettings().setBuffer(settings.isBuffer());

			s.setScrollMarks(settings.isScrollMarks());
			s.setCustomCss(settings.getCustomCss());
			s.setCustomJs(settings.getCustomJs());
			s.setLanguage(settings.getLanguage());
			s.setScrollSpeed(settings.getScrollSpeed());
			s.setAlwaysScrollToEntry(settings.isAlwaysScrollToEntry());
			s.setMarkAllAsReadConfirmation(settings.isMarkAllAsReadConfirmation());
			s.setCustomContextMenu(settings.isCustomContextMenu());
		} else {
			s.setReadingMode(ReadingMode.unread.name());
			s.setReadingOrder(ReadingOrder.desc.name());
			s.setShowRead(true);

			s.getSharingSettings().setEmail(true);
			s.getSharingSettings().setGmail(true);
			s.getSharingSettings().setFacebook(true);
			s.getSharingSettings().setTwitter(true);
			s.getSharingSettings().setTumblr(true);
			s.getSharingSettings().setPocket(true);
			s.getSharingSettings().setInstapaper(true);
			s.getSharingSettings().setBuffer(true);

			s.setScrollMarks(true);
			s.setLanguage("en");
			s.setScrollSpeed(400);
			s.setAlwaysScrollToEntry(false);
			s.setMarkAllAsReadConfirmation(true);
			s.setCustomContextMenu(true);
		}
		return Response.ok(s).build();
	}

	@Path("/settings")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Save user settings", notes = "Save user settings")
	@Timed
	public Response saveUserSettings(@ApiParam(hidden = true) @SecurityCheck User user, @ApiParam(required = true) Settings settings) {
		Preconditions.checkNotNull(settings);

		UserSettings s = userSettingsDAO.findByUser(user);
		if (s == null) {
			s = new UserSettings();
			s.setUser(user);
		}
		s.setReadingMode(ReadingMode.valueOf(settings.getReadingMode()));
		s.setReadingOrder(ReadingOrder.valueOf(settings.getReadingOrder()));
		s.setShowRead(settings.isShowRead());
		s.setScrollMarks(settings.isScrollMarks());
		s.setCustomCss(settings.getCustomCss());
		s.setCustomJs(settings.getCustomJs());
		s.setLanguage(settings.getLanguage());
		s.setScrollSpeed(settings.getScrollSpeed());
		s.setAlwaysScrollToEntry(settings.isAlwaysScrollToEntry());
		s.setMarkAllAsReadConfirmation(settings.isMarkAllAsReadConfirmation());
		s.setCustomContextMenu(settings.isCustomContextMenu());

		s.setEmail(settings.getSharingSettings().isEmail());
		s.setGmail(settings.getSharingSettings().isGmail());
		s.setFacebook(settings.getSharingSettings().isFacebook());
		s.setTwitter(settings.getSharingSettings().isTwitter());
		s.setTumblr(settings.getSharingSettings().isTumblr());
		s.setPocket(settings.getSharingSettings().isPocket());
		s.setInstapaper(settings.getSharingSettings().isInstapaper());
		s.setBuffer(settings.getSharingSettings().isBuffer());

		userSettingsDAO.saveOrUpdate(s);
		return Response.ok().build();

	}

	@Path("/profile")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Retrieve user's profile", response = UserModel.class)
	@Timed
	public Response getUserProfile(@ApiParam(hidden = true) @SecurityCheck User user) {
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
	public Response saveUserProfile(@ApiParam(hidden = true) @SecurityCheck User user,
			@Valid @ApiParam(required = true) ProfileModificationRequest request) {
		if (CommaFeedApplication.USERNAME_DEMO.equals(user.getName())) {
			return Response.status(Status.FORBIDDEN).build();
		}

		Optional<User> login = userService.login(user.getEmail(), request.getCurrentPassword());
		if (!login.isPresent()) {
			throw new BadRequestException("invalid password");
		}

		String email = StringUtils.trimToNull(request.getEmail());
		if (StringUtils.isNotBlank(email)) {
			User u = userDAO.findByEmail(email);
			if (u != null && !user.getId().equals(u.getId())) {
				throw new BadRequestException("email already taken");
			}
		}
		user.setEmail(email);

		if (StringUtils.isNotBlank(request.getNewPassword())) {
			byte[] password = encryptionService.getEncryptedPassword(request.getNewPassword(), user.getSalt());
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
	public Response registerUser(@Valid @ApiParam(required = true) RegistrationRequest req,
			@Context @ApiParam(hidden = true) SessionHelper sessionHelper) {
		try {
			User registeredUser = userService.register(req.getName(), req.getPassword(), req.getEmail(),
					Collections.singletonList(Role.USER));
			userService.login(req.getName(), req.getPassword());
			sessionHelper.setLoggedInUser(registeredUser);
			return Response.ok().build();
		} catch (final IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@Path("/login")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Login and create a session")
	@Timed
	public Response login(@Valid @ApiParam(required = true) LoginRequest req,
			@ApiParam(hidden = true) @Context SessionHelper sessionHelper) {
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
	public Response sendPasswordReset(@Valid @ApiParam(required = true) PasswordResetRequest req) {
		User user = userDAO.findByEmail(req.getEmail());
		if (user == null) {
			return Response.ok().build();
		}

		try {
			user.setRecoverPasswordToken(DigestUtils.sha1Hex(UUID.randomUUID().toString()));
			user.setRecoverPasswordTokenDate(new Date());
			userDAO.saveOrUpdate(user);
			mailService.sendMail(user, "Password recovery", buildEmailContent(user));
			return Response.ok().build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("could not send email").type(MediaType.TEXT_PLAIN).build();
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
		return new URIBuilder(publicUrl).addParameter("email", user.getEmail())
				.addParameter("token", user.getRecoverPasswordToken())
				.build()
				.toURL()
				.toString();
	}

	@Path("/passwordResetCallback")
	@GET
	@UnitOfWork
	@Produces(MediaType.TEXT_HTML)
	@Timed
	public Response passwordRecoveryCallback(@ApiParam(required = true) @QueryParam("email") String email,
			@ApiParam(required = true) @QueryParam("token") String token) {
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
	public Response deleteUser(@ApiParam(hidden = true) @SecurityCheck User user) {
		if (CommaFeedApplication.USERNAME_ADMIN.equals(user.getName()) || CommaFeedApplication.USERNAME_DEMO.equals(user.getName())) {
			return Response.status(Status.FORBIDDEN).build();
		}
		userService.unregister(userDAO.findById(user.getId()));
		return Response.ok().build();
	}
}
