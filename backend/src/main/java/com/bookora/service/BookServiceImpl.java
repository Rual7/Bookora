package com.bookora.service;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.dto.BookDetailDto;
import com.bookora.dto.BookSummaryDto;
import com.bookora.dto.PageDto;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.Book;
import com.bookora.model.BookSort;
import com.bookora.model.ProductFormat;
import com.bookora.model.Review;
import com.bookora.model.ReviewStatus;
import com.bookora.repository.BookRepository;
import com.bookora.repository.RatingSummary;
import com.bookora.repository.ReviewRepository;

@Service
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

	private final BookRepository books;
	private final ReviewRepository reviews;
	private final BookMapper mapper;

	public BookServiceImpl(BookRepository books, ReviewRepository reviews, BookMapper mapper) {
		this.books = books;
		this.reviews = reviews;
		this.mapper = mapper;
	}

	@Override
	public List<BookSummaryDto> findFeatured() {
		return books.findByFeaturedTrueAndActiveTrueOrderByTitleAsc().stream().map(mapper::toSummary).toList();
	}

	@Override
	public PageDto<BookSummaryDto> search(String query, String category, String format, BookSort sort,
			Pageable pageable) {
		String like = isBlank(query) ? "" : "%" + escapeLike(query.trim().toLowerCase(Locale.ROOT)) + "%";
		List<ProductFormat> formats = formatsFor(format);
		Page<Book> found = books.search(like, isBlank(category) ? "" : category.trim(), formats, sort.name(), pageable);
		return PageDto.of(found.map(book -> mapper.toSummary(book, formats)));
	}

	/** "ebook" and "printed" are the groups the website offers; anything else means all formats. */
	private static List<ProductFormat> formatsFor(String format) {
		if (format == null) {
			return List.of(ProductFormat.values());
		}
		return switch (format.trim().toLowerCase(Locale.ROOT)) {
			case "ebook" -> List.of(ProductFormat.PDF, ProductFormat.EPUB);
			case "printed" -> List.of(ProductFormat.PAPERBACK, ProductFormat.HARDCOVER);
			default -> List.of(ProductFormat.values());
		};
	}

	@Override
	public List<BookSummaryDto> findByIds(List<Long> ids) {
		return books.findAllById(ids).stream()
				.filter(Book::isActive)
				.map(mapper::toSummary)
				.toList();
	}

	@Override
	public BookDetailDto getBySlug(String slug) {
		Book book = books.findBySlugAndActiveTrue(slug)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found: " + slug));
		RatingSummary rating = books.findRating(book.getId()).orElse(null);
		List<Review> approved = reviews.findByBookIdAndStatusOrderByCreatedAtDesc(book.getId(), ReviewStatus.APPROVED);
		return mapper.toDetail(book, rating, approved);
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private static String escapeLike(String value) {
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

}
