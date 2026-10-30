package com.bookora.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BookoraException {

	public BadRequestException(String message) {
		super(HttpStatus.BAD_REQUEST, message);
	}

}
