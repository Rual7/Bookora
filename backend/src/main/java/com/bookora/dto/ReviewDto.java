package com.bookora.dto;

import java.time.Instant;

public record ReviewDto(int rating, String title, String comment, String authorName, Instant createdAt) {
}
