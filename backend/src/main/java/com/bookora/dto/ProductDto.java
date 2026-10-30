package com.bookora.dto;

import java.math.BigDecimal;

import com.bookora.model.ProductFormat;

public record ProductDto(
		Long id,
		String sku,
		ProductFormat format,
		String isbn,
		BigDecimal price,
		String currency,
		boolean inStock) {
}
