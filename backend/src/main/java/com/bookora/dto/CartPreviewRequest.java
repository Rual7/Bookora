package com.bookora.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CartPreviewRequest(@NotNull @Size(max = 50) List<@Valid CartItemRequest> items) {
}
