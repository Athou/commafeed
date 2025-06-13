package com.commafeed.frontend.resource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;

import com.commafeed.CommaFeedApplication;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.Digests;
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
import com.commafeed.frontend.model.Settings;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.PasswordResetRequest;
import com.commafeed.frontend.model.request.ProfileModificationRequest;
import com.commafeed.frontend.model.request.RegistrationRequest;
import com.commafeed.security.AuthenticationContext;
import com.commafeed.security.Roles;
import com.google.common.base.Preconditions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Path("/rest/user")
@RolesAllowed(Roles.USER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@RequiredArgsConstructor
@Singleton
@Tag(name = "Users")
public class UserREST {

	private final AuthenticationContext authenticationContext;
	private final UserDAO userDAO;
	private final UserRoleDAO userRoleDAO;
	private final UserSettingsDAO userSettingsDAO;
	private final UserService userService;
	private final PasswordEncryptionService encryptionService;
	private final MailService mailService;
	private final CommaFeedConfiguration config;
	private final UriInfo uri;

	@Path("/settings")
	@GET
	@Transactional
	@Operation(
			summary = "Retrieve user settings",
			description = "Retrieve user settings",
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Settings.class))) })
	public Response getUserSettings() {
		Settings s = new Settings();

		User user = authenticationContext.getCurrentUser();
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
			s.setEntriesToKeepOnTopWhenScrolling(settings.getEntriesToKeepOnTopWhenScrolling());
			s.setStarIconDisplayMode(settings.getStarIconDisplayMode().name());
			s.setExternalLinkIconDisplayMode(settings.getExternalLinkIconDisplayMode().name());
			s.setMarkAllAsReadConfirmation(settings.isMarkAllAsReadConfirmation());
			s.setMarkAllAsReadNavigateToNextUnread(settings.isMarkAllAsReadNavigateToNextUnread());
			s.setCustomContextMenu(settings.isCustomContextMenu());
			s.setMobileFooter(settings.isMobileFooter());
			s.setUnreadCountTitle(settings.isUnreadCountTitle());
			s.setUnreadCountFavicon(settings.isUnreadCountFavicon());
			s.setPrimaryColor(settings.getPrimaryColor());
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
			s.setEntriesToKeepOnTopWhenScrolling(1);
			s.setStarIconDisplayMode(IconDisplayMode.on_desktop.name());
			s.setExternalLinkIconDisplayMode(IconDisplayMode.on_desktop.name());
			s.setMarkAllAsReadConfirmation(true);
			s.setMarkAllAsReadNavigateToNextUnread(false);
			s.setCustomContextMenu(true);
			s.setMobileFooter(false);
			s.setUnreadCountTitle(false);
			s.setUnreadCountFavicon(true);
		}
		return Response.ok(s).build();
	}

	@Path("/settings")
	@POST
	@Transactional
	@Operation(summary = "Save user settings", description = "Save user settings")
	public Response saveUserSettings(@Parameter(required = true) Settings settings) {
		Preconditions.checkNotNull(settings);

		User user = authenticationContext.getCurrentUser();
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
		s.setEntriesToKeepOnTopWhenScrolling(settings.getEntriesToKeepOnTopWhenScrolling());
		s.setStarIconDisplayMode(IconDisplayMode.valueOf(settings.getStarIconDisplayMode()));
		s.setExternalLinkIconDisplayMode(IconDisplayMode.valueOf(settings.getExternalLinkIconDisplayMode()));
		s.setMarkAllAsReadConfirmation(settings.isMarkAllAsReadConfirmation());
		s.setMarkAllAsReadNavigateToNextUnread(settings.isMarkAllAsReadNavigateToNextUnread());
		s.setCustomContextMenu(settings.isCustomContextMenu());
		s.setMobileFooter(settings.isMobileFooter());
		s.setUnreadCountTitle(settings.isUnreadCountTitle());
		s.setUnreadCountFavicon(settings.isUnreadCountFavicon());
		s.setPrimaryColor(settings.getPrimaryColor());

		s.setEmail(settings.getSharingSettings().isEmail());
		s.setGmail(settings.getSharingSettings().isGmail());
		s.setFacebook(settings.getSharingSettings().isFacebook());
		s.setTwitter(settings.getSharingSettings().isTwitter());
		s.setTumblr(settings.getSharingSettings().isTumblr());
		s.setPocket(settings.getSharingSettings().isPocket());
		s.setInstapaper(settings.getSharingSettings().isInstapaper());
		s.setBuffer(settings.getSharingSettings().isBuffer());

		userSettingsDAO.merge(s);
		return Response.ok().build();

	}

	@Path("/profile")
	@GET
	@Transactional
	@Operation(
			summary = "Retrieve user's profile",
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = UserModel.class))) })
	public Response getUserProfile() {
		User user = authenticationContext.getCurrentUser();

		UserModel userModel = new UserModel();
		userModel.setId(user.getId());
		userModel.setName(user.getName());
		userModel.setEmail(user.getEmail());
		userModel.setEnabled(!user.isDisabled());
		userModel.setApiKey(user.getApiKey());
		userModel.setLastForceRefresh(user.getLastForceRefresh());
		for (UserRole role : userRoleDAO.findAll(user)) {
			if (role.getRole() == Role.ADMIN) {
				userModel.setAdmin(true);
			}
		}
		return Response.ok(userModel).build();
	}

	@Path("/profile")
	@POST
	@Transactional
	@Operation(summary = "Save user's profile")
	public Response saveUserProfile(@Valid @Parameter(required = true) ProfileModificationRequest request) {
		User user = authenticationContext.getCurrentUser();
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

		userDAO.merge(user);
		return Response.ok().build();
	}

	@Path("/register")
	@PermitAll
	@POST
	@Transactional
	@Operation(summary = "Register a new account")
	public Response registerUser(@Valid @Parameter(required = true) RegistrationRequest req) {
		try {
			userService.register(req.getName(), req.getPassword(), req.getEmail(), Collections.singletonList(Role.USER));
			return Response.ok().build();
		} catch (final IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@Path("/passwordReset")
	@PermitAll
	@POST
	@Transactional
	@Operation(summary = "send a password reset email")
	public Response sendPasswordReset(@Valid @Parameter(required = true) PasswordResetRequest req) {
		if (!config.passwordRecoveryEnabled()) {
			throw new IllegalArgumentException("Password recovery is not enabled on this CommaFeed instance");
		}

		User user = userDAO.findByEmail(req.getEmail());
		if (user == null) {
			return Response.ok().build();
		}

		try {
			user.setRecoverPasswordToken(Digests.sha1Hex(UUID.randomUUID().toString()));
			user.setRecoverPasswordTokenDate(Instant.now());

			mailService.sendMail(user, "Password recovery", buildEmailContent(user));
			return Response.ok().build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("could not send email").type(MediaType.TEXT_PLAIN).build();
		}
	}

	private String buildEmailContent(User user) throws Exception {
		String publicUrl = FeedUtils.removeTrailingSlash(uri.getBaseUri().toString());
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
	@PermitAll
	@GET
	@Transactional
	@Produces(MediaType.TEXT_HTML)
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

		String passwd = RandomStringUtils.secure().nextAlphanumeric(10);
		byte[] encryptedPassword = encryptionService.getEncryptedPassword(passwd, user.getSalt());
		user.setPassword(encryptedPassword);
		if (StringUtils.isNotBlank(user.getApiKey())) {
			user.setApiKey(userService.generateApiKey(user));
		}
		user.setRecoverPasswordToken(null);
		user.setRecoverPasswordTokenDate(null);

		String message = "Your new password is: " + passwd;
		message += "<br />";
		message += String.format("<a href=\"%s\">Back to Homepage</a>", uri.getBaseUri());
		return Response.ok(message).build();
	}

	@Path("/profile/deleteAccount")
	@POST
	@Transactional
	@Operation(summary = "Delete the user account")
	public Response deleteUser() {
		User user = authenticationContext.getCurrentUser();
		if (CommaFeedApplication.USERNAME_ADMIN.equals(user.getName()) || CommaFeedApplication.USERNAME_DEMO.equals(user.getName())) {
			return Response.status(Status.FORBIDDEN).build();
		}
		userService.unregister(userDAO.findById(user.getId()));
		return Response.ok().build();
	}
}
