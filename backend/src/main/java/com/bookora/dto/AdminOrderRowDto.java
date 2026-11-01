package com.bookora.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.bookora.model.OrderStatus;

public record AdminOrderRowDto(String orderNumber, String customerName, String customerEmail, OrderStatus status,
		Instant createdAt, int itemCount, BigDecimal total, String currency) {
}
