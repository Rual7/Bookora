package com.bookora.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
public class Product extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "book_id", nullable = false)
	private Book book;

	@Column(nullable = false, unique = true)
	private String sku;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProductFormat format;

	private String isbn;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(name = "stock_quantity", nullable = false)
	private int stockQuantity;

	@Column(nullable = false)
	private boolean active = true;

	protected Product() {
	}

	public Book getBook() {
		return book;
	}

	public String getSku() {
		return sku;
	}

	public ProductFormat getFormat() {
		return format;
	}

	public String getIsbn() {
		return isbn;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public int getStockQuantity() {
		return stockQuantity;
	}

	public boolean isActive() {
		return active;
	}

}
