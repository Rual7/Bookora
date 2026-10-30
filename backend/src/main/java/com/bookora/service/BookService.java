package com.bookora.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.bookora.dto.BookDetailDto;
import com.bookora.dto.BookSummaryDto;
import com.bookora.dto.PageDto;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.BookSort;

public interface BookService {

	List<BookSummaryDto> findFeatured();

	/** Blank filters are ignored. The page is only used for its number and size; {@code sort} decides the order. */
	PageDto<BookSummaryDto> search(String query, String category, String format, BookSort sort, Pageable pageable);

	/** For a visitor's wishlist kept in the browser. Unknown or inactive books are left out. */
	List<BookSummaryDto> findByIds(List<Long> ids);

	/** @throws ResourceNotFoundException if no active book has this slug */
	BookDetailDto getBySlug(String slug);

}
