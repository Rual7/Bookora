package com.bookora.dto;

import java.time.Instant;
import java.util.List;

import com.bookora.model.ProductFormat;

/** An e-book the customer paid for. {@code available} is false while the file is missing or did not pass the check. */
public record LibraryItemDto(Long orderItemId, String title, String bookSlug, String coverImagePath,
		List<String> authors, ProductFormat format, Instant purchasedAt, boolean available) {
}
