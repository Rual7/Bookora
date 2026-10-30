package com.bookora.dto;

import java.math.BigDecimal;
import java.util.List;

import com.bookora.model.ProductFormat;

public record CartItemDto(
		Long productId,
		Long bookId,
		String bookSlug,
		String title,
		List<String> authors,
		String coverImagePath,
		ProductFormat format,
		BigDecimal unitPrice,
		int quantity,
		BigDecimal lineTotal,
		/** Highest quantity the customer can buy: stock for printed books, 1 for e-books. */
		int maxQuantity) {
}
