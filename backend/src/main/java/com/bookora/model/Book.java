package com.bookora.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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

	@OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("position ASC")
	private List<BookImage> images = new ArrayList<>();

	protected Book() {
	}

	public Book(String title, String slug) {
		this.title = title;
		this.slug = slug;
	}

	public void update(String title, String description, String publisher, LocalDate publishedOn, String languageCode,
			Integer pageCount, boolean featured, boolean active) {
		this.title = title;
		this.description = description;
		this.publisher = publisher;
		this.publishedOn = publishedOn;
		this.languageCode = languageCode;
		this.pageCount = pageCount;
		this.featured = featured;
		this.active = active;
	}

	public void addImage(String path) {
		images.add(new BookImage(this, path, images.size()));
		syncCover();
	}

	/** Removes the image and returns its path, or null if this book has no such image. */
	public String removeImage(Long imageId) {
		BookImage image = findImage(imageId);
		if (image == null) {
			return null;
		}
		images.remove(image);
		renumber();
		return image.getPath();
	}

	/** Moves the image to the front, so it becomes the cover. Returns false if this book has no such image. */
	public boolean makeMainImage(Long imageId) {
		BookImage image = findImage(imageId);
		if (image == null) {
			return false;
		}
		images.remove(image);
		images.add(0, image);
		renumber();
		return true;
	}

	private BookImage findImage(Long imageId) {
		return images.stream().filter(image -> image.getId().equals(imageId)).findFirst().orElse(null);
	}

	private void renumber() {
		for (int i = 0; i < images.size(); i++) {
			images.get(i).setPosition(i);
		}
		syncCover();
	}

	private void syncCover() {
		coverImagePath = images.isEmpty() ? null : images.get(0).getPath();
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

	public List<BookImage> getImages() {
		return images;
	}

}
