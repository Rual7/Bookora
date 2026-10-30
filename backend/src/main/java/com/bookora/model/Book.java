package com.bookora.model;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "book")
public class Book extends BaseEntity {

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, unique = true)
	private String slug;

	@Column(columnDefinition = "text")
	private String description;

	@Column(name = "cover_image_path")
	private String coverImagePath;

	private String publisher;

	@Column(name = "published_on")
	private LocalDate publishedOn;

	@Column(name = "language_code")
	private String languageCode;

	@Column(name = "page_count")
	private Integer pageCount;

	@Column(nullable = false)
	private boolean featured;

	@Column(nullable = false)
	private boolean active = true;

	@ManyToMany
	@JoinTable(name = "book_author",
			joinColumns = @JoinColumn(name = "book_id"),
			inverseJoinColumns = @JoinColumn(name = "author_id"))
	private Set<Author> authors = new LinkedHashSet<>();

	@ManyToMany
	@JoinTable(name = "book_category",
			joinColumns = @JoinColumn(name = "book_id"),
			inverseJoinColumns = @JoinColumn(name = "category_id"))
	private Set<Category> categories = new LinkedHashSet<>();

	@OneToMany(mappedBy = "book")
	private Set<Product> products = new LinkedHashSet<>();

	protected Book() {
	}

	public String getTitle() {
		return title;
	}

	public String getSlug() {
		return slug;
	}

	public String getDescription() {
		return description;
	}

	public String getCoverImagePath() {
		return coverImagePath;
	}

	public String getPublisher() {
		return publisher;
	}

	public LocalDate getPublishedOn() {
		return publishedOn;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public Integer getPageCount() {
		return pageCount;
	}

	public boolean isFeatured() {
		return featured;
	}

	public boolean isActive() {
		return active;
	}

	public Set<Author> getAuthors() {
		return authors;
	}

	public Set<Category> getCategories() {
		return categories;
	}

	public Set<Product> getProducts() {
		return products;
	}

}
