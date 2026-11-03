package com.bookora.dto;

import com.bookora.model.Category;

public record CategoryDto(Long id, String name, String slug, String description) {

	public static CategoryDto from(Category category) {
		return new CategoryDto(category.getId(), category.getName(), category.getSlug(), category.getDescription());
	}

}
