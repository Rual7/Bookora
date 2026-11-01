package com.bookora.service;

import java.util.Locale;

/** Turns what a visitor typed into a SQL LIKE pattern that matches it anywhere, with the wildcards in it made literal. */
final class LikePattern {

	private LikePattern() {
	}

	/** An empty string means "no filter", so queries never bind a null. */
	static String contains(String typed) {
		if (typed == null || typed.isBlank()) {
			return "";
		}
		String lower = typed.trim().toLowerCase(Locale.ROOT);
		return "%" + lower.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
	}

}
