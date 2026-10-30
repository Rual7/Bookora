package com.bookora.service;

import java.util.List;

import com.bookora.dto.CartDto;
import com.bookora.dto.CartItemRequest;

/** Every method works on the cart of the user with the given email and returns the updated cart. */
public interface CartService {

	CartDto getCart(String email);

	CartDto addItem(String email, Long productId, int quantity);

	CartDto setQuantity(String email, Long productId, int quantity);

	CartDto removeItem(String email, Long productId);

	/** Prices a cart kept in the visitor's browser, without saving anything. */
	CartDto preview(List<CartItemRequest> items);

}
