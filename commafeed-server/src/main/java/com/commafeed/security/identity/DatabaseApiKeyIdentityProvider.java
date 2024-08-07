package com.commafeed.security.identity;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.UserService;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Singleton
public class DatabaseApiKeyIdentityProvider implements IdentityProvider<TokenAuthenticationRequest> {

	private final UnitOfWork unitOfWork;
	private final UserService userService;

	@Override
	public Class<TokenAuthenticationRequest> getRequestType() {
		return TokenAuthenticationRequest.class;
	}

	@Override
	public Uni<SecurityIdentity> authenticate(TokenAuthenticationRequest request, AuthenticationRequestContext context) {
		return context.runBlocking(() -> {
			Optional<User> user = unitOfWork.call(() -> userService.login(request.getToken().getToken()));
			if (user.isEmpty()) {
				throw new AuthenticationFailedException("could not find a user with this api key");
			}

			Set<Role> roles = unitOfWork.call(() -> userService.getRoles(user.get()));
			return QuarkusSecurityIdentity.builder()
					.setPrincipal(new QuarkusPrincipal(String.valueOf(user.get().getId())))
					.addRoles(roles.stream().map(Enum::name).collect(Collectors.toSet()))
					.build();
		});
	}
}
