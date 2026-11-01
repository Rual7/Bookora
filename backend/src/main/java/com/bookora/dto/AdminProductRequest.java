package com.bookora.dto;

import java.math.BigDecimal;

import com.bookora.model.ProductFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminProductRequest(
		@NotNull(message = "Choose a format") ProductFormat format,
		@Size(max = 20, message = "Use at most 20 characters") String isbn,
		@NotNull(message = "Enter a price")
		@DecimalMin(value = "0.00", message = "The price cannot be negative")
		@Digits(integer = 8, fraction = 2, message = "Use at most two decimals") BigDecimal price,
		@Min(value = 0, message = "The stock cannot be negative") int stockQuantity,
		boolean active) {
}
