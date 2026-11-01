package com.bookora.dto;

import java.math.BigDecimal;

import com.bookora.model.ProductFormat;

public record OrderItemDto(
		Long id,
		String title,
		ProductFormat format,
		int quantity,
		BigDecimal unitPrice,
		BigDecimal lineTotal) {
}
