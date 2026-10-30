package com.bookora.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.dto.BookSummaryDto;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.AppUser;
import com.bookora.model.Book;
import com.bookora.model.WishlistItem;
import com.bookora.model.WishlistItemId;
import com.bookora.repository.BookRepository;
import com.bookora.repository.UserRepository;
import com.bookora.repository.WishlistRepository;

@Service
@Transactional
public class WishlistServiceImpl implements WishlistService {

	private final WishlistRepository wishlist;
	private final BookRepository books;
	private final UserRepository users;
	private final BookMapper mapper;

	public WishlistServiceImpl(WishlistRepository wishlist, BookRepository books, UserRepository users,
			BookMapper mapper) {
		this.wishlist = wishlist;
		this.books = books;
		this.users = users;
		this.mapper = mapper;
	}

	@Override
	@Transactional(readOnly = true)
	public List<BookSummaryDto> list(String email) {
		return wishlist.findByIdUserId(findUser(email).getId()).stream()
				.map(WishlistItem::getBook)
				.filter(Book::isActive)
				.map(mapper::toSummary)
				.toList();
	}

	@Override
	public void add(String email, Long bookId) {
		AppUser user = findUser(email);
		if (wishlist.existsById(new WishlistItemId(user.getId(), bookId))) {
			return;
		}
		Book book = books.findById(bookId)
				.filter(Book::isActive)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found"));
		wishlist.save(new WishlistItem(user, book));
	}

	@Override
	public void remove(String email, Long bookId) {
		AppUser user = findUser(email);
		wishlist.deleteById(new WishlistItemId(user.getId(), bookId));
	}

	private AppUser findUser(String email) {
		return users.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

}
