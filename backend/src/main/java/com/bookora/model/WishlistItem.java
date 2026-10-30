package com.bookora.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "wishlist_item")
public class WishlistItem {

	@EmbeddedId
	private WishlistItemId id;

	@MapsId("userId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private AppUser user;

	@MapsId("bookId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "book_id")
	private Book book;

	protected WishlistItem() {
	}

	public WishlistItem(AppUser user, Book book) {
		this.id = new WishlistItemId(user.getId(), book.getId());
		this.user = user;
		this.book = book;
	}

	public Book getBook() {
		return book;
	}

}
