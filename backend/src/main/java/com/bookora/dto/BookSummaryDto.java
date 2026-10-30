package com.bookora.dto;

import java.math.BigDecimal;
import java.util.List;

public record BookSummaryDto(
		Long id,
		String title,
		String slug,
		String coverImagePath,
		List<String> authors,
		BigDecimal price,
		String currency,
		Availability availability) {

	public enum Availability {
		IN_STOCK,
		OUT_OF_STOCK,
		INSTANT_DOWNLOAD
	}

}
