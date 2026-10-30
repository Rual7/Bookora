package com.bookora.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.dto.BookSummaryDto;
import com.bookora.service.WishlistService;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

	private final WishlistService wishlistService;

	public WishlistController(WishlistService wishlistService) {
		this.wishlistService = wishlistService;
	}

	@GetMapping
	public List<BookSummaryDto> list(Authentication auth) {
		return wishlistService.list(auth.getName());
	}

	@PutMapping("/{bookId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void add(Authentication auth, @PathVariable Long bookId) {
		wishlistService.add(auth.getName(), bookId);
	}

	@DeleteMapping("/{bookId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remove(Authentication auth, @PathVariable Long bookId) {
		wishlistService.remove(auth.getName(), bookId);
	}

}
