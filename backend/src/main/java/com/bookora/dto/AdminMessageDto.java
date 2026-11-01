package com.bookora.dto;

import java.time.Instant;

public record AdminMessageDto(Long id, String name, String email, String message, boolean handled, Instant createdAt) {
}
