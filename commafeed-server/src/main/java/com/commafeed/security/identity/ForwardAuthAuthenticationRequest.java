package com.commafeed.security.identity;

import io.quarkus.security.identity.request.BaseAuthenticationRequest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/** Authentication request built from the headers sent by a trusted reverse proxy. */
@Getter
@RequiredArgsConstructor
public class ForwardAuthAuthenticationRequest extends BaseAuthenticationRequest {

    /** Attribute set on security identities authenticated by the reverse proxy. */
    public static final String IDENTITY_ATTRIBUTE = "commafeed.forward-auth";

    private final String username;
    private final String email;
    private final Set<String> groups;
}
