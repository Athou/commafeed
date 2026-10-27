package com.commafeed.security.mechanism;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.CommaFeedConfiguration.ForwardAuth;
import com.commafeed.security.identity.ForwardAuthAuthenticationRequest;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.BasicAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authenticates requests using the user name header set by a reverse proxy performing forward
 * authentication against a single sign-on provider.
 *
 * <p>The header is only trusted if the request comes directly from one of the configured trusted
 * proxies. The address of the TCP connection is used for this check, not the client address that
 * may have been rewritten from the X-Forwarded-For header.
 */
@Slf4j
@Singleton
public class ForwardAuthAuthenticationMechanism implements HttpAuthenticationMechanism {

    // run before the other mechanisms so that the user sent by the proxy takes precedence over a
    // session cookie of another user
    private static final int PRIORITY = BasicAuthenticationMechanism.PRIORITY + 1000;

    private final ForwardAuth config;
    private final List<IPAddress> trustedProxies;

    public ForwardAuthAuthenticationMechanism(CommaFeedConfiguration config) {
        this.config = config.forwardAuth();
        this.trustedProxies =
                this.config.trustedProxies().orElse(List.of()).stream()
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .map(ForwardAuthAuthenticationMechanism::parseTrustedProxy)
                        .toList();

        if (this.config.enabled() && this.trustedProxies.isEmpty()) {
            log.warn(
                    "forward authentication is enabled but no trusted proxies are configured,"
                            + " the {} header will be ignored",
                    this.config.usernameHeader());
        }
    }

    @Override
    public Uni<SecurityIdentity> authenticate(
            RoutingContext context, IdentityProviderManager identityProviderManager) {
        if (!config.enabled()) {
            return Uni.createFrom().optional(Optional.empty());
        }

        String username =
                StringUtils.trimToNull(context.request().getHeader(config.usernameHeader()));
        if (username == null) {
            return Uni.createFrom().optional(Optional.empty());
        }

        SocketAddress remoteAddress = context.request().connection().remoteAddress();
        if (!isTrustedProxy(remoteAddress)) {
            log.warn(
                    "ignoring {} header from untrusted address {}",
                    config.usernameHeader(),
                    remoteAddress);
            return Uni.createFrom().optional(Optional.empty());
        }

        String email = StringUtils.trimToNull(context.request().getHeader(config.emailHeader()));
        Set<String> groups =
                Arrays.stream(
                                StringUtils.defaultString(
                                                context.request().getHeader(config.groupsHeader()))
                                        .split(","))
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toSet());

        return identityProviderManager.authenticate(
                new ForwardAuthAuthenticationRequest(username, email, groups));
    }

    private boolean isTrustedProxy(SocketAddress remoteAddress) {
        if (remoteAddress == null || remoteAddress.hostAddress() == null) {
            return false;
        }

        IPAddress address = new IPAddressString(remoteAddress.hostAddress()).getAddress();
        if (address == null) {
            return false;
        }

        if (address.isIPv6() && address.toIPv6().isIPv4Mapped()) {
            address = address.toIPv6().getEmbeddedIPv4Address();
        }

        IPAddress finalAddress = address;
        return trustedProxies.stream().anyMatch(range -> range.contains(finalAddress));
    }

    private static IPAddress parseTrustedProxy(String value) {
        IPAddress address = new IPAddressString(value).getAddress();
        if (address == null) {
            throw new IllegalArgumentException(
                    "invalid trusted proxy address for forward authentication: " + value);
        }
        return address.toPrefixBlock();
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().optional(Optional.empty());
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Set.of(ForwardAuthAuthenticationRequest.class);
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }
}
