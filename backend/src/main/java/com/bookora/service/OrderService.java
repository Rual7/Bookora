package com.bookora.service;

import java.util.List;

import com.bookora.dto.OrderDto;

public interface OrderService {

	/** Turns the customer's saved cart into an order, takes the stock and empties the cart. */
	OrderDto checkout(String email, Long addressId);

	List<OrderDto> list(String email);

	OrderDto get(String email, String orderNumber);

}
