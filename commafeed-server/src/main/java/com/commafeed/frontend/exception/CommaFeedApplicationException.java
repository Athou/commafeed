package com.commafeed.frontend.exception;

import java.io.Serial;
import java.util.Objects;

public class CommaFeedApplicationException extends RuntimeException {

    @Serial private static final long serialVersionUID = 1L;

    private final CommaFeedExceptionType type;

    public CommaFeedApplicationException(CommaFeedExceptionType type) {
        super(Objects.requireNonNull(type).message());
        this.type = type;
    }

    public CommaFeedExceptionType type() {
        return type;
    }
}
