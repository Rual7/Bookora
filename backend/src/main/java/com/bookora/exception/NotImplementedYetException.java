package com.bookora.exception;

import org.springframework.http.HttpStatus;

public class NotImplementedYetException extends BookoraException {

	public NotImplementedYetException(String feature) {
		super(HttpStatus.NOT_IMPLEMENTED, feature + " is not implemented yet");
	}

}
