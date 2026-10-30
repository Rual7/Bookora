package com.bookora.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart")
public class Cart extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private AppUser user;

	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CartItem> items = new ArrayList<>();

	protected Cart() {
	}

	public Cart(AppUser user) {
		this.user = user;
	}

	public List<CartItem> getItems() {
		return items;
	}

	public CartItem findItem(Long productId) {
		return items.stream()
				.filter(item -> item.getProduct().getId().equals(productId))
				.findFirst()
				.orElse(null);
	}

	public void addItem(Product product, int quantity) {
		items.add(new CartItem(this, product, quantity));
	}

	public void removeItem(Long productId) {
		items.removeIf(item -> item.getProduct().getId().equals(productId));
	}

}
