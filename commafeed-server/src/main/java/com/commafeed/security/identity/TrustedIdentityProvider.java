package com.commafeed.security.identity;

import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.UserService;
import com.commafeed.backend.service.internal.PostLoginActivities;
import com.commafeed.security.SessionPrincipal;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TrustedAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;

import jakarta.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Singleton
public class TrustedIdentityProvider implements IdentityProvider<TrustedAuthenticationRequest> {

    private final UnitOfWork unitOfWork;
    private final UserService userService;
    private final UserDAO userDAO;
    private final PostLoginActivities postLoginActivities;

    @Override
    public Class<TrustedAuthenticationRequest> getRequestType() {
        return TrustedAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(
            TrustedAuthenticationRequest request, AuthenticationRequestContext context) {
        return context.runBlocking(() -> {
            SessionPrincipal principal = SessionPrincipal.parse(request.getPrincipal());
            User user = unitOfWork.call(() -> userDAO.findById(principal.userId()));
            if (user == null) {
                throw new AuthenticationFailedException("user not found");
            }
            if (user.isDisabled()) {
                throw new AuthenticationFailedException("user disabled");
            }
            if (user.getSessionVersion() != principal.sessionVersion()) {
                throw new AuthenticationFailedException("session invalidated");
            }

            // execute post login activities manually because we didn't call login() since
            // we received
            // a trusted authentication request
            unitOfWork.run(() -> postLoginActivities.executeFor(user));

            Set<Role> roles = unitOfWork.call(() -> userService.getRoles(user));
            return QuarkusSecurityIdentity.builder()
                    .setPrincipal(new QuarkusPrincipal(principal.toName()))
                    .addRoles(roles.stream().map(Enum::name).collect(Collectors.toSet()))
                    .build();
        });
    }
}
