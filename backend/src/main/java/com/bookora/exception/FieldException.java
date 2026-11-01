package com.bookora.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

/** A request that is refused because of one specific field; the field name is sent back so the form can show the message under it. */
public class FieldException extends BookoraException {

	private final Map<String, String> errors;

	public FieldException(String field, String message) {
		super(HttpStatus.BAD_REQUEST, message);
		this.errors = Map.of(field, message);
	}

	public Map<String, String> getErrors() {
		return errors;
	}

}
