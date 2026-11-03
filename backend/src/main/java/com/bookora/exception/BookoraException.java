package com.bookora.exception;

import org.springframework.http.HttpStatus;

/** Base of all application errors; each subclass fixes the HTTP status it maps to. The list of subclasses is closed. */
public abstract sealed class BookoraException extends RuntimeException
		permits BadRequestException, ConflictException, FieldException, ResourceNotFoundException,
		ServiceUnavailableException {

	private final HttpStatus status;

	protected BookoraException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}

}
