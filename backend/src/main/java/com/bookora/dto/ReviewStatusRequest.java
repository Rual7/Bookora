package com.bookora.dto;

import com.bookora.model.ReviewStatus;

import jakarta.validation.constraints.NotNull;

public record ReviewStatusRequest(@NotNull ReviewStatus status) {
}
