package com.bookora.dto;

import java.math.BigDecimal;

import com.bookora.model.ProductFormat;

public record AdminProductDto(Long id, String sku, ProductFormat format, String isbn, BigDecimal price,
		int stockQuantity, boolean active, EbookFileDto ebook) {
}
