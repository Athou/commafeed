package com.commafeed.security.identity;

import java.util.Set;
import java.util.stream.Collectors;

import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.UserService;

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

@RequiredArgsConstructor
@Singleton
public class TrustedIdentityProvider implements IdentityProvider<TrustedAuthenticationRequest> {

	private final UnitOfWork unitOfWork;
	private final UserService userService;
	private final UserDAO userDAO;

	@Override
	public Class<TrustedAuthenticationRequest> getRequestType() {
		return TrustedAuthenticationRequest.class;
	}

	@Override
	public Uni<SecurityIdentity> authenticate(TrustedAuthenticationRequest request, AuthenticationRequestContext context) {
		return context.runBlocking(() -> {
			Long userId = Long.valueOf(request.getPrincipal());
			User user = unitOfWork.call(() -> userDAO.findById(userId));
			if (user == null) {
				throw new AuthenticationFailedException("user not found");
			}

			Set<Role> roles = unitOfWork.call(() -> userService.getRoles(user));
			return QuarkusSecurityIdentity.builder()
					.setPrincipal(new QuarkusPrincipal(String.valueOf(userId)))
					.addRoles(roles.stream().map(Enum::name).collect(Collectors.toSet()))
					.build();
		});
	}
}
