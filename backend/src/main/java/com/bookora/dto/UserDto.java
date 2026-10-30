package com.bookora.dto;

import com.bookora.model.Role;

public record UserDto(Long id, String email, String firstName, String lastName, Role role) {
}
