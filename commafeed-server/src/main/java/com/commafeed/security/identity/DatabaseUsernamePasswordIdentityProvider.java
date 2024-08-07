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
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Singleton
public class DatabaseUsernamePasswordIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

	private final UnitOfWork unitOfWork;
	private final UserService userService;

	@Override
	public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
		return UsernamePasswordAuthenticationRequest.class;
	}

	@Override
	public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request, AuthenticationRequestContext context) {
		return context.runBlocking(() -> {
			Optional<User> user = unitOfWork
					.call(() -> userService.login(request.getUsername(), new String(request.getPassword().getPassword())));
			if (user.isEmpty()) {
				throw new AuthenticationFailedException("wrong username or password");
			}

			Set<Role> roles = unitOfWork.call(() -> userService.getRoles(user.get()));
			return QuarkusSecurityIdentity.builder()
					.setPrincipal(new QuarkusPrincipal(String.valueOf(user.get().getId())))
					.addRoles(roles.stream().map(Enum::name).collect(Collectors.toSet()))
					.build();
		});
	}
}
