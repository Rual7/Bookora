package com.bookora.service;

/** Small helpers for text that comes from forms. */
final class Text {

	private Text() {
	}

	/** Optional fields are stored as null, not as an empty or blank string. */
	static String trimToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

}
