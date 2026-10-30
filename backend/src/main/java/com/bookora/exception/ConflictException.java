package com.bookora.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BookoraException {

	public ConflictException(String message) {
		super(HttpStatus.CONFLICT, message);
	}

}
