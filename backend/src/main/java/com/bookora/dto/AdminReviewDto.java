package com.bookora.dto;

import java.time.Instant;

import com.bookora.model.ReviewStatus;

public record AdminReviewDto(Long id, String bookTitle, String bookSlug, String authorName, String authorEmail,
		int rating, String title, String comment, ReviewStatus status, boolean verifiedPurchase, Instant createdAt) {
}
