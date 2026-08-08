package com.commafeed.security.mechanism;

import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

import jakarta.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.Set;

/**
 * Authenticates requests to the Google Reader compatible API, using the "Authorization: GoogleLogin
 * auth=&lt;token&gt;" header set by Google Reader API clients.
 *
 * <p>This mechanism only applies to requests under {@link #PATH_PREFIX}, so that a stray
 * "Authorization: GoogleLogin ..." header sent to any other endpoint is not mistakenly accepted as
 * an api key authentication.
 */
@Singleton
public class GoogleReaderAuthenticationMechanism implements HttpAuthenticationMechanism {

    public static final String PATH_PREFIX = "/rest/googlereader";

    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_PREFIX = "GoogleLogin auth=";

    @Override
    public Uni<SecurityIdentity> authenticate(
            RoutingContext context, IdentityProviderManager identityProviderManager) {
        if (!context.normalizedPath().startsWith(PATH_PREFIX)) {
            return Uni.createFrom().optional(Optional.empty());
        }

        String header = context.request().getHeader(AUTH_HEADER);
        if (StringUtils.isBlank(header) || !header.startsWith(AUTH_PREFIX)) {
            return Uni.createFrom().optional(Optional.empty());
        }

        String token = header.substring(AUTH_PREFIX.length()).trim();
        int slashIndex = token.lastIndexOf('/');
        String apiKey = slashIndex == -1 ? token : token.substring(slashIndex + 1);
        if (StringUtils.isBlank(apiKey)) {
            return Uni.createFrom().optional(Optional.empty());
        }

        TokenCredential credential = new TokenCredential(apiKey, "apiKey");
        TokenAuthenticationRequest request = new TokenAuthenticationRequest(credential);
        return identityProviderManager.authenticate(request);
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().optional(Optional.empty());
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Set.of(TokenAuthenticationRequest.class);
    }
}
