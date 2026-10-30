package com.bookora.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart_item")
public class CartItem {

	@EmbeddedId
	private CartItemId id;

	@MapsId("cartId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cart_id")
	private Cart cart;

	@MapsId("productId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id")
	private Product product;

	private int quantity;

	protected CartItem() {
	}

	CartItem(Cart cart, Product product, int quantity) {
		this.id = new CartItemId(cart.getId(), product.getId());
		this.cart = cart;
		this.product = product;
		this.quantity = quantity;
	}

	public Product getProduct() {
		return product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

}
