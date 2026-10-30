package com.bookora.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.dto.CheckoutRequest;
import com.bookora.dto.OrderDto;
import com.bookora.service.OrderService;

@RestController
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping("/api/checkout")
	@ResponseStatus(HttpStatus.CREATED)
	public OrderDto checkout(Authentication auth, @RequestBody CheckoutRequest request) {
		return orderService.checkout(auth.getName(), request.addressId());
	}

	@GetMapping("/api/orders")
	public List<OrderDto> list(Authentication auth) {
		return orderService.list(auth.getName());
	}

	@GetMapping("/api/orders/{orderNumber}")
	public OrderDto get(Authentication auth, @PathVariable String orderNumber) {
		return orderService.get(auth.getName(), orderNumber);
	}

}
