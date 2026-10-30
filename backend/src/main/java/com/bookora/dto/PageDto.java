package com.bookora.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/** Our own page shape, so the JSON does not depend on Spring Data internals. */
public record PageDto<T>(List<T> content, int page, int totalPages, long totalElements) {

	public static <T> PageDto<T> of(Page<T> page) {
		return new PageDto<>(page.getContent(), page.getNumber(), page.getTotalPages(), page.getTotalElements());
	}

}
