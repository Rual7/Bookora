package com.bookora.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.dto.BookDetailDto;
import com.bookora.dto.BookSummaryDto;
import com.bookora.dto.PageDto;
import com.bookora.model.BookSort;
import com.bookora.service.BookService;

@RestController
@RequestMapping("/api/books")
public class BookController {

	private static final int MAX_PAGE_SIZE = 50;

	private final BookService bookService;

	public BookController(BookService bookService) {
		this.bookService = bookService;
	}

	@GetMapping
	public PageDto<BookSummaryDto> list(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) String format,
			@RequestParam(defaultValue = "title") String sort,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "12") int size) {
		int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
		return bookService.search(q, category, format, BookSort.from(sort),
				PageRequest.of(Math.max(page, 0), safeSize));
	}

	@GetMapping("/featured")
	public List<BookSummaryDto> featured() {
		return bookService.findFeatured();
	}

	@GetMapping("/by-ids")
	public List<BookSummaryDto> byIds(@RequestParam List<Long> ids) {
		return bookService.findByIds(ids.size() > MAX_PAGE_SIZE * 2 ? ids.subList(0, MAX_PAGE_SIZE * 2) : ids);
	}

	@GetMapping("/{slug}")
	public BookDetailDto detail(@PathVariable String slug) {
		return bookService.getBySlug(slug);
	}

}
