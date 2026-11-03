package com.bookora.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.dto.ReviewRequest;
import com.bookora.exception.ConflictException;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.AppUser;
import com.bookora.model.Book;
import com.bookora.model.Review;
import com.bookora.repository.BookRepository;
import com.bookora.repository.ReviewRepository;
import com.bookora.repository.UserRepository;

@Service
public class ReviewServiceImpl implements ReviewService {

	private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

	private final ReviewRepository reviews;
	private final BookRepository books;
	private final UserRepository users;

	public ReviewServiceImpl(ReviewRepository reviews, BookRepository books, UserRepository users) {
		this.reviews = reviews;
		this.books = books;
		this.users = users;
	}

	@Override
	@Transactional
	public void submit(String email, String bookSlug, ReviewRequest request) {
		AppUser user = users.requireByEmail(email);
		Book book = books.findBySlugAndActiveTrue(bookSlug)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found"));
		if (reviews.existsByUserIdAndBookId(user.getId(), book.getId())) {
			throw new ConflictException("You have already reviewed this book");
		}
		reviews.save(new Review(user, book, request.rating(), Text.trimToNull(request.title()), request.comment().trim()));
		log.info("Review submitted: userId={}, bookId={}", user.getId(), book.getId());
	}

}
