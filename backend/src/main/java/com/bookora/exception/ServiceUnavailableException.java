package com.bookora.exception;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends BookoraException {

	public ServiceUnavailableException(String message) {
		super(HttpStatus.SERVICE_UNAVAILABLE, message);
	}

}
