package com.bookora.dto;

/** The address is only needed when the order contains printed books. */
public record CheckoutRequest(Long addressId) {
}
