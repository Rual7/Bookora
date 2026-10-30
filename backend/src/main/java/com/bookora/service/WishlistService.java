package com.bookora.service;

import java.util.List;

import com.bookora.dto.BookSummaryDto;

public interface WishlistService {

	List<BookSummaryDto> list(String email);

	/** Adding a book that is already on the wishlist does nothing. */
	void add(String email, Long bookId);

	void remove(String email, Long bookId);

}
