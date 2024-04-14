package com.commafeed.frontend.resource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;

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
import com.commafeed.backend.model.UserSettings.IconDisplayMode;
import com.commafeed.backend.model.UserSettings.ReadingMode;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.backend.model.UserSettings.ScrollMode;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
@Tag(name = "Users")
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
	@Operation(
			summary = "Retrieve user settings",
			description = "Retrieve user settings",
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Settings.class))) })
	@Timed
	public Response getUserSettings(@Parameter(hidden = true) @SecurityCheck User user) {
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
			s.setScrollMode(settings.getScrollMode().name());
			s.setStarIconDisplayMode(settings.getStarIconDisplayMode().name());
			s.setExternalLinkIconDisplayMode(settings.getExternalLinkIconDisplayMode().name());
			s.setMarkAllAsReadConfirmation(settings.isMarkAllAsReadConfirmation());
			s.setCustomContextMenu(settings.isCustomContextMenu());
			s.setMobileFooter(settings.isMobileFooter());
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
			s.setScrollMode(ScrollMode.if_needed.name());
			s.setStarIconDisplayMode(IconDisplayMode.always.name());
			s.setExternalLinkIconDisplayMode(IconDisplayMode.always.name());
			s.setMarkAllAsReadConfirmation(true);
			s.setCustomContextMenu(true);
			s.setMobileFooter(false);
		}
		return Response.ok(s).build();
	}

	@Path("/settings")
	@POST
	@UnitOfWork
	@Operation(summary = "Save user settings", description = "Save user settings")
	@Timed
	public Response saveUserSettings(@Parameter(hidden = true) @SecurityCheck User user, @Parameter(required = true) Settings settings) {
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
		s.setCustomJs(CommaFeedApplication.USERNAME_DEMO.equals(user.getName()) ? "" : settings.getCustomJs());
		s.setLanguage(settings.getLanguage());
		s.setScrollSpeed(settings.getScrollSpeed());
		s.setScrollMode(ScrollMode.valueOf(settings.getScrollMode()));
		s.setStarIconDisplayMode(IconDisplayMode.valueOf(settings.getStarIconDisplayMode()));
		s.setExternalLinkIconDisplayMode(IconDisplayMode.valueOf(settings.getExternalLinkIconDisplayMode()));
		s.setMarkAllAsReadConfirmation(settings.isMarkAllAsReadConfirmation());
		s.setCustomContextMenu(settings.isCustomContextMenu());
		s.setMobileFooter(settings.isMobileFooter());

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
	@Operation(
			summary = "Retrieve user's profile",
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = UserModel.class))) })
	@Timed
	public Response getUserProfile(@Parameter(hidden = true) @SecurityCheck User user) {
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
	@Operation(summary = "Save user's profile")
	@Timed
	public Response saveUserProfile(@Parameter(hidden = true) @SecurityCheck User user,
			@Valid @Parameter(required = true) ProfileModificationRequest request) {
		if (CommaFeedApplication.USERNAME_DEMO.equals(user.getName())) {
			return Response.status(Status.FORBIDDEN).build();
		}

		Optional<User> login = userService.login(user.getName(), request.getCurrentPassword());
		if (login.isEmpty()) {
			throw new BadRequestException("invalid password");
		}

		String email = StringUtils.trimToNull(request.getEmail());
		if (StringUtils.isNotBlank(email)) {
			User u = userDAO.findByEmail(email);
			if (u != null && !user.getId().equals(u.getId())) {
				throw new BadRequestException("email already taken");
			}
			user.setEmail(email);
		}

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
	@Operation(summary = "Register a new account")
	@Timed
	public Response registerUser(@Valid @Parameter(required = true) RegistrationRequest req,
			@Context @Parameter(hidden = true) SessionHelper sessionHelper) {
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
	@Operation(summary = "Login and create a session")
	@Timed
	public Response login(@Valid @Parameter(required = true) LoginRequest req,
			@Parameter(hidden = true) @Context SessionHelper sessionHelper) {
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
	@Operation(summary = "send a password reset email")
	@Timed
	public Response sendPasswordReset(@Valid @Parameter(required = true) PasswordResetRequest req) {
		User user = userDAO.findByEmail(req.getEmail());
		if (user == null) {
			return Response.ok().build();
		}

		try {
			user.setRecoverPasswordToken(DigestUtils.sha1Hex(UUID.randomUUID().toString()));
			user.setRecoverPasswordTokenDate(Instant.now());
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
	public Response passwordRecoveryCallback(@Parameter(required = true) @QueryParam("email") String email,
			@Parameter(required = true) @QueryParam("token") String token) {
		Preconditions.checkNotNull(email);
		Preconditions.checkNotNull(token);

		User user = userDAO.findByEmail(email);
		if (user == null) {
			return Response.status(Status.UNAUTHORIZED).entity("Email not found.").build();
		}
		if (user.getRecoverPasswordToken() == null || !user.getRecoverPasswordToken().equals(token)) {
			return Response.status(Status.UNAUTHORIZED).entity("Invalid token.").build();
		}
		if (ChronoUnit.DAYS.between(user.getRecoverPasswordTokenDate(), Instant.now()) >= 2) {
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
	@Operation(summary = "Delete the user account")
	@Timed
	public Response deleteUser(@Parameter(hidden = true) @SecurityCheck User user) {
		if (CommaFeedApplication.USERNAME_ADMIN.equals(user.getName()) || CommaFeedApplication.USERNAME_DEMO.equals(user.getName())) {
			return Response.status(Status.FORBIDDEN).build();
		}
		userService.unregister(userDAO.findById(user.getId()));
		return Response.ok().build();
	}
}
