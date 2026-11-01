package com.bookora.dto;

import java.time.Instant;

import com.bookora.model.Role;

public record AdminCustomerDto(Long id, String name, String email, Role role, boolean enabled, int orderCount,
		Instant createdAt) {
}
