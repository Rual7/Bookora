package com.bookora.model;

import java.util.Locale;

/** The ways the catalog can be ordered. The website sends them as lower-case words with dashes. */
public enum BookSort {
	TITLE,
	NEWEST,
	PRICE_ASC,
	PRICE_DESC,
	RATING;

	/** Unknown or missing values fall back to title order. */
	public static BookSort from(String value) {
		if (value == null) {
			return TITLE;
		}
		try {
			return valueOf(value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
		} catch (IllegalArgumentException unknown) {
			return TITLE;
		}
	}
}
