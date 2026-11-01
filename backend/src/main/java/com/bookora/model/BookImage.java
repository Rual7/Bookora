package com.bookora.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One picture of a book. The one with position 0 is the cover shown in lists. */
@Entity
@Table(name = "book_image")
public class BookImage extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "book_id", nullable = false)
	private Book book;

	@Column(nullable = false)
	private String path;

	@Column(nullable = false)
	private int position;

	protected BookImage() {
	}

	BookImage(Book book, String path, int position) {
		this.book = book;
		this.path = path;
		this.position = position;
	}

	void setPosition(int position) {
		this.position = position;
	}

	public Book getBook() {
		return book;
	}

	public String getPath() {
		return path;
	}

	public int getPosition() {
		return position;
	}

}
