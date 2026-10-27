package com.commafeed.security;

import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;
import com.commafeed.security.identity.ForwardAuthAuthenticationRequest;

import io.quarkus.security.identity.SecurityIdentity;

import jakarta.inject.Singleton;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Singleton
public class AuthenticationContext {

    private final SecurityIdentity securityIdentity;
    private final UserDAO userDAO;

    public User getCurrentUser() {
        if (securityIdentity.isAnonymous()) {
            return null;
        }

        String userId = securityIdentity.getPrincipal().getName();
        if (userId == null) {
            return null;
        }

        return userDAO.findById(Long.valueOf(userId));
    }

    /** whether the current user has been authenticated by a trusted reverse proxy */
    public boolean isForwardAuthenticated() {
        return Boolean.TRUE.equals(
                securityIdentity.getAttribute(ForwardAuthAuthenticationRequest.IDENTITY_ATTRIBUTE));
    }
}
