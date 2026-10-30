package com.bookora.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.dto.CartDto;
import com.bookora.dto.CartItemRequest;
import com.bookora.dto.CartPreviewRequest;
import com.bookora.dto.QuantityRequest;
import com.bookora.service.CartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
public class CartController {

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	/** Open to visitors: they keep their cart in the browser and only need it priced. */
	@PostMapping("/preview")
	public CartDto preview(@Valid @RequestBody CartPreviewRequest request) {
		return cartService.preview(request.items());
	}

	@GetMapping
	public CartDto get(Authentication auth) {
		return cartService.getCart(auth.getName());
	}

	@PostMapping("/items")
	public CartDto add(Authentication auth, @Valid @RequestBody CartItemRequest request) {
		return cartService.addItem(auth.getName(), request.productId(), request.quantity());
	}

	@PutMapping("/items/{productId}")
	public CartDto changeQuantity(Authentication auth, @PathVariable Long productId,
			@Valid @RequestBody QuantityRequest request) {
		return cartService.setQuantity(auth.getName(), productId, request.quantity());
	}

	@DeleteMapping("/items/{productId}")
	public CartDto remove(Authentication auth, @PathVariable Long productId) {
		return cartService.removeItem(auth.getName(), productId);
	}

}
