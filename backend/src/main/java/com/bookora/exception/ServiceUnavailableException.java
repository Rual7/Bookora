package com.bookora.exception;

import org.springframework.http.HttpStatus;

public final class ServiceUnavailableException extends BookoraException {

	public ServiceUnavailableException(String message) {
		super(HttpStatus.SERVICE_UNAVAILABLE, message);
	}

}
