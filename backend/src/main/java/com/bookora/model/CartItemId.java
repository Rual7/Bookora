package com.bookora.model;

import java.io.Serializable;

import jakarta.persistence.Embeddable;

@Embeddable
public record CartItemId(Long cartId, Long productId) implements Serializable {
}
