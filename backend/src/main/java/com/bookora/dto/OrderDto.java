package com.bookora.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.bookora.model.OrderStatus;

/** shippingAddress is null for orders that only contain e-books. */
public record OrderDto(
		String orderNumber,
		OrderStatus status,
		Instant createdAt,
		Instant paidAt,
		List<OrderItemDto> items,
		int itemCount,
		BigDecimal shippingCost,
		BigDecimal total,
		String currency,
		ShippingAddressDto shippingAddress) {
}
