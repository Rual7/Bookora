package com.bookora.dto;

import java.time.LocalDate;
import java.util.List;

public record AdminBookDto(Long id, String title, String slug, String description, String publisher,
		LocalDate publishedOn, String languageCode, Integer pageCount, boolean featured, boolean active,
		String coverImagePath, List<BookImageDto> images, List<String> authors, List<Long> categoryIds,
		List<AdminProductDto> products) {
}
