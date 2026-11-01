package com.bookora.dto;

import java.math.BigDecimal;
import java.util.List;

/** {@code standardShipping} and {@code freeShippingAbove} describe the delivery rule, so the page never repeats the numbers itself. */
public record CartDto(List<CartItemDto> items, int itemCount, BigDecimal subtotal, BigDecimal shippingCost,
		BigDecimal total, String currency, BigDecimal standardShipping, BigDecimal freeShippingAbove) {
}
