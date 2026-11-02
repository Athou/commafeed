package com.commafeed.security;

import com.commafeed.backend.model.User;

/** Value of the security principal, which is also what ends up in the encrypted session cookie. */
public record SessionPrincipal(long userId, int sessionVersion) {

    private static final String SEPARATOR = ":";

    public static SessionPrincipal of(User user) {
        return new SessionPrincipal(user.getId(), user.getSessionVersion());
    }

    public static SessionPrincipal parse(String name) {
        // cookies created before session versions were introduced only contain the user id
        if (!name.contains(SEPARATOR)) {
            return new SessionPrincipal(Long.parseLong(name), 0);
        }

        String[] parts = name.split(SEPARATOR, 2);
        return new SessionPrincipal(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
    }

    public String toName() {
        return userId + SEPARATOR + sessionVersion;
    }
}
