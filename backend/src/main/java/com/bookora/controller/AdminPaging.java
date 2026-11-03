package com.bookora.controller;

/** Every administration list shows the same number of rows per page. */
final class AdminPaging {

	static final int PAGE_SIZE = 20;

	private AdminPaging() {
	}

	/** A negative page number from the address bar means the first page. */
	static int page(int requested) {
		return Math.max(requested, 0);
	}

}
