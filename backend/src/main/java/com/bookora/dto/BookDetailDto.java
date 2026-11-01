package com.bookora.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** {@code images} lists every picture of the book, the cover first; it is empty when none was uploaded. */
public record BookDetailDto(
		Long id,
		String title,
		String slug,
		String description,
		String coverImagePath,
		List<String> images,
		String publisher,
		LocalDate publishedOn,
		String languageCode,
		Integer pageCount,
		List<String> authors,
		List<CategoryDto> categories,
		List<ProductDto> products,
		BigDecimal averageRating,
		long reviewCount,
		List<ReviewDto> reviews) {
}
