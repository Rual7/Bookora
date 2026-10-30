package com.bookora.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One line of an order. Title and price are copied, so the order stays the same if the catalog changes. */
@Entity
@Table(name = "order_item")
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private CustomerOrder order;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "product_title", nullable = false)
	private String productTitle;

	@Enumerated(EnumType.STRING)
	@Column(name = "product_format", nullable = false)
	private ProductFormat productFormat;

	private int quantity;

	@Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	protected OrderItem() {
	}

	OrderItem(CustomerOrder order, Product product, int quantity) {
		this.order = order;
		this.product = product;
		this.productTitle = product.getBook().getTitle();
		this.productFormat = product.getFormat();
		this.quantity = quantity;
		this.unitPrice = product.getPrice();
	}

	public Product getProduct() {
		return product;
	}

	public String getProductTitle() {
		return productTitle;
	}

	public ProductFormat getProductFormat() {
		return productFormat;
	}

	public int getQuantity() {
		return quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public BigDecimal getLineTotal() {
		return unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

}
