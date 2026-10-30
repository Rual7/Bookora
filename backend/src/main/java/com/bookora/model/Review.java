package com.bookora.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "review")
public class Review extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "book_id", nullable = false)
	private Book book;

	@Column(nullable = false)
	private short rating;

	private String title;

	@Column(columnDefinition = "text")
	private String comment;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReviewStatus status = ReviewStatus.PENDING;

	protected Review() {
	}

	public AppUser getUser() {
		return user;
	}

	public short getRating() {
		return rating;
	}

	public String getTitle() {
		return title;
	}

	public String getComment() {
		return comment;
	}

	public ReviewStatus getStatus() {
		return status;
	}

}
