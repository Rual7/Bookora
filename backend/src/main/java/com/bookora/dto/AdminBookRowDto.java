package com.bookora.dto;

import java.util.List;

import com.bookora.model.ProductFormat;

public record AdminBookRowDto(Long id, String title, String slug, String coverImagePath, List<String> authors,
		List<ProductFormat> formats, boolean featured, boolean active) {
}
