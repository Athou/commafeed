package com.commafeed.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SessionPrincipalTest {

    @Test
    void roundTrip() {
        SessionPrincipal principal = new SessionPrincipal(12, 3);
        Assertions.assertEquals("12:3", principal.toName());
        Assertions.assertEquals(principal, SessionPrincipal.parse(principal.toName()));
    }

    @Test
    void cookieWithoutSessionVersion() {
        Assertions.assertEquals(new SessionPrincipal(12, 0), SessionPrincipal.parse("12"));
    }
}
