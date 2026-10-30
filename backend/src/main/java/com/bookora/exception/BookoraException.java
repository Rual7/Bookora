package com.bookora.exception;

import org.springframework.http.HttpStatus;

/** Base of all application errors; each subclass fixes the HTTP status it maps to. */
public abstract class BookoraException extends RuntimeException {

	private final HttpStatus status;

	protected BookoraException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}

}
