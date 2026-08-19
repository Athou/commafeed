package com.commafeed;

import org.jboss.resteasy.reactive.RestResponse.Status;

public enum CommaFeedExceptionType {
    WRONG_USERNAME_OR_PASSWORD(Status.UNAUTHORIZED, "wrong username or password");

    private final Status status;
    private final String message;

    CommaFeedExceptionType(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status status() {
        return status;
    }

    public String message() {
        return message;
    }
}
