package com.commafeed.security.identity;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.UserService;
import com.commafeed.backend.service.internal.PostLoginActivities;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;

import jakarta.inject.Singleton;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Resolves the user sent by a trusted reverse proxy, creating the user account on first login if
 * enabled.
 */
@Slf4j
@RequiredArgsConstructor
@Singleton
public class ForwardAuthIdentityProvider
        implements IdentityProvider<ForwardAuthAuthenticationRequest> {

    // see the length of the USERS.name and USERS.email columns
    private static final int MAX_USERNAME_LENGTH = 32;
    private static final int MAX_EMAIL_LENGTH = 255;

    private final UnitOfWork unitOfWork;
    private final UserService userService;
    private final UserDAO userDAO;
    private final UserRoleDAO userRoleDAO;
    private final PostLoginActivities postLoginActivities;
    private final CommaFeedConfiguration config;

    @Override
    public Class<ForwardAuthAuthenticationRequest> getRequestType() {
        return ForwardAuthAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(
            ForwardAuthAuthenticationRequest request, AuthenticationRequestContext context) {
        return context.runBlocking(
                () -> {
                    User user = unitOfWork.call(() -> userDAO.findByName(request.getUsername()));
                    if (user == null) {
                        user = createUser(request);
                    }

                    // the user may have been disabled by an admin of CommaFeed while still being
                    // able to log in to the single sign-on provider
                    if (user.isDisabled()) {
                        throw new AuthenticationFailedException(
                                "user " + user.getName() + " is disabled");
                    }

                    User finalUser = user;
                    syncAdminRole(finalUser, request.getGroups());
                    unitOfWork.run(() -> postLoginActivities.executeFor(finalUser));

                    Set<Role> roles = unitOfWork.call(() -> userService.getRoles(finalUser));
                    return QuarkusSecurityIdentity.builder()
                            .setPrincipal(new QuarkusPrincipal(String.valueOf(user.getId())))
                            .addRoles(roles.stream().map(Enum::name).collect(Collectors.toSet()))
                            .addAttribute(ForwardAuthAuthenticationRequest.IDENTITY_ATTRIBUTE, true)
                            .build();
                });
    }

    // synchronized because the web client sends several requests in parallel on first load, which
    // would otherwise all try to create the same user
    private synchronized User createUser(ForwardAuthAuthenticationRequest request) {
        return unitOfWork.call(
                () -> {
                    User existing = userDAO.findByName(request.getUsername());
                    if (existing != null) {
                        return existing;
                    }

                    if (!config.forwardAuth().autoCreateUsers()) {
                        throw new AuthenticationFailedException(
                                "user " + request.getUsername() + " does not exist");
                    }

                    if (request.getUsername().length() > MAX_USERNAME_LENGTH) {
                        throw new AuthenticationFailedException(
                                "user name "
                                        + request.getUsername()
                                        + " is longer than "
                                        + MAX_USERNAME_LENGTH
                                        + " characters");
                    }

                    Set<Role> roles = EnumSet.of(Role.USER);
                    if (isAdmin(request.getGroups()).orElse(false)) {
                        roles.add(Role.ADMIN);
                    }

                    log.info(
                            "creating user {} authenticated by reverse proxy",
                            request.getUsername());
                    return userService.register(
                            request.getUsername(),
                            UUID.randomUUID().toString(),
                            availableEmail(request.getEmail()),
                            roles,
                            true);
                });
    }

    private String availableEmail(String email) {
        if (email == null || email.length() > MAX_EMAIL_LENGTH) {
            return null;
        }
        return userDAO.findByEmail(email) == null ? email : null;
    }

    private void syncAdminRole(User user, Set<String> groups) {
        Optional<Boolean> admin = isAdmin(groups);
        if (admin.isEmpty()) {
            return;
        }

        boolean hasAdminRole =
                unitOfWork.call(() -> userRoleDAO.findRoles(user)).contains(Role.ADMIN);
        if (admin.get() == hasAdminRole) {
            return;
        }

        synchronized (this) {
            unitOfWork.run(
                    () -> {
                        Set<Role> roles = userRoleDAO.findRoles(user);
                        if (admin.get() && !roles.contains(Role.ADMIN)) {
                            log.info("granting admin role to {}", user.getName());
                            userRoleDAO.persist(new UserRole(user, Role.ADMIN));
                        } else if (!admin.get()
                                && roles.contains(Role.ADMIN)
                                && userRoleDAO.countAdmins() > 1) {
                            log.info("revoking admin role from {}", user.getName());
                            userRoleDAO.findAll(user).stream()
                                    .filter(r -> r.getRole() == Role.ADMIN)
                                    .forEach(userRoleDAO::delete);
                        }
                    });
        }
    }

    /** empty if admin roles are not managed by the reverse proxy */
    private Optional<Boolean> isAdmin(Set<String> groups) {
        return config.forwardAuth().adminGroup().map(groups::contains);
    }
}
