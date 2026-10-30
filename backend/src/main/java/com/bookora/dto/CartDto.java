package com.bookora.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(List<CartItemDto> items, int itemCount, BigDecimal subtotal, BigDecimal shippingCost,
		BigDecimal total, String currency) {
}
