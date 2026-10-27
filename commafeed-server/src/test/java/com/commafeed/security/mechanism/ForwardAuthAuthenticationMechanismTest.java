package com.commafeed.security.mechanism;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.security.identity.ForwardAuthAuthenticationRequest;

import io.quarkus.security.identity.IdentityProviderManager;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ForwardAuthAuthenticationMechanismTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CommaFeedConfiguration config;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RoutingContext context;

    @Mock private IdentityProviderManager identityProviderManager;

    @BeforeEach
    void init() {
        Mockito.when(config.forwardAuth().enabled()).thenReturn(true);
        Mockito.when(config.forwardAuth().usernameHeader()).thenReturn("Remote-User");
        Mockito.when(config.forwardAuth().emailHeader()).thenReturn("Remote-Email");
        Mockito.when(config.forwardAuth().groupsHeader()).thenReturn("Remote-Groups");
        Mockito.when(config.forwardAuth().trustedProxies())
                .thenReturn(Optional.of(List.of("10.0.0.0/8", " 192.168.1.10 ", "fd00::/8")));

        Mockito.when(context.request().getHeader("Remote-User")).thenReturn(" john ");
        Mockito.when(context.request().getHeader("Remote-Email")).thenReturn("john@example.com");
        Mockito.when(context.request().getHeader("Remote-Groups")).thenReturn("users, admins,");
    }

    @ParameterizedTest
    @ValueSource(strings = {"10.1.2.3", "192.168.1.10", "fd12::1", "::ffff:10.0.0.1"})
    void trustedProxy(String address) {
        remoteAddress(address);

        newMechanism().authenticate(context, identityProviderManager);

        ArgumentCaptor<ForwardAuthAuthenticationRequest> captor =
                ArgumentCaptor.forClass(ForwardAuthAuthenticationRequest.class);
        Mockito.verify(identityProviderManager).authenticate(captor.capture());
        Assertions.assertEquals("john", captor.getValue().getUsername());
        Assertions.assertEquals("john@example.com", captor.getValue().getEmail());
        Assertions.assertEquals(Set.of("users", "admins"), captor.getValue().getGroups());
    }

    @ParameterizedTest
    @ValueSource(strings = {"11.0.0.1", "192.168.1.11", "127.0.0.1", "::1", "::ffff:11.0.0.1"})
    void untrustedProxy(String address) {
        remoteAddress(address);

        Assertions.assertNull(
                newMechanism()
                        .authenticate(context, identityProviderManager)
                        .await()
                        .indefinitely());
        Mockito.verifyNoInteractions(identityProviderManager);
    }

    @Test
    void noHeader() {
        remoteAddress("10.0.0.1");
        Mockito.when(context.request().getHeader("Remote-User")).thenReturn(" ");

        Assertions.assertNull(
                newMechanism()
                        .authenticate(context, identityProviderManager)
                        .await()
                        .indefinitely());
        Mockito.verifyNoInteractions(identityProviderManager);
    }

    @Test
    void disabled() {
        remoteAddress("10.0.0.1");
        Mockito.when(config.forwardAuth().enabled()).thenReturn(false);

        Assertions.assertNull(
                newMechanism()
                        .authenticate(context, identityProviderManager)
                        .await()
                        .indefinitely());
        Mockito.verifyNoInteractions(identityProviderManager);
    }

    @Test
    void noTrustedProxies() {
        remoteAddress("10.0.0.1");
        Mockito.when(config.forwardAuth().trustedProxies()).thenReturn(Optional.empty());

        Assertions.assertNull(
                newMechanism()
                        .authenticate(context, identityProviderManager)
                        .await()
                        .indefinitely());
        Mockito.verifyNoInteractions(identityProviderManager);
    }

    @Test
    void invalidTrustedProxy() {
        Mockito.when(config.forwardAuth().trustedProxies())
                .thenReturn(Optional.of(List.of("not-an-ip")));

        Assertions.assertThrows(IllegalArgumentException.class, this::newMechanism);
    }

    private void remoteAddress(String address) {
        SocketAddress socketAddress = Mockito.mock(SocketAddress.class);
        Mockito.when(socketAddress.hostAddress()).thenReturn(address);
        Mockito.when(context.request().connection().remoteAddress()).thenReturn(socketAddress);
    }

    private ForwardAuthAuthenticationMechanism newMechanism() {
        return new ForwardAuthAuthenticationMechanism(config);
    }
}
