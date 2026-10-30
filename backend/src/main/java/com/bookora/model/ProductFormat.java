package com.bookora.model;

public enum ProductFormat {
	PAPERBACK(false),
	HARDCOVER(false),
	PDF(true),
	EPUB(true);

	private final boolean digital;

	ProductFormat(boolean digital) {
		this.digital = digital;
	}

	public boolean isDigital() {
		return digital;
	}
}
