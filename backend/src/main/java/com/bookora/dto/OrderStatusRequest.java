package com.bookora.dto;

import com.bookora.model.OrderStatus;

import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(@NotNull OrderStatus status) {
}
