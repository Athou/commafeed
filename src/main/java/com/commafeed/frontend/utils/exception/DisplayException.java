package com.commafeed.frontend.utils.exception;

public class DisplayException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private DisplayException() {

	}

	public DisplayException(String message) {
		super(message, new DisplayException());
	}

	public DisplayException(Throwable t) {
		super(t.getMessage(), t);
	}

	public DisplayException(String message, Throwable t) {
		super(message, t);
	}

}
