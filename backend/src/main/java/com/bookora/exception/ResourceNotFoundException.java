package com.bookora.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BookoraException {

	public ResourceNotFoundException(String message) {
		super(HttpStatus.NOT_FOUND, message);
	}

}
