package com.bookora.exception;

import org.springframework.http.HttpStatus;

public final class ConflictException extends BookoraException {

	public ConflictException(String message) {
		super(HttpStatus.CONFLICT, message);
	}

}
