package com.bookora.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record AdminBookRequest(
		@NotBlank(message = "Enter a title") @Size(max = 255, message = "Use at most 255 characters") String title,
		@Size(max = 5000, message = "Use at most 5000 characters") String description,
		@Size(max = 200, message = "Use at most 200 characters") String publisher,
		LocalDate publishedOn,
		@Size(max = 10, message = "Use at most 10 characters") String languageCode,
		@Min(value = 1, message = "A book has at least one page") Integer pageCount,
		boolean featured,
		boolean active,
		@NotEmpty(message = "Add at least one author")
		List<@NotBlank(message = "Add at least one author") @Size(max = 200, message = "Use at most 200 characters") String> authors,
		List<Long> categoryIds,
		@Valid @NotEmpty(message = "Sell the book in at least one format") List<AdminProductRequest> products) {
}
