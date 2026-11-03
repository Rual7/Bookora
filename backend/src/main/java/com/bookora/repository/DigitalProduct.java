package com.bookora.repository;

import com.bookora.model.ProductFormat;

/** An e-book product on sale and the slug of its book. */
public record DigitalProduct(Long productId, String bookSlug, ProductFormat format) {
}
