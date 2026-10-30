package com.bookora.model;

import java.io.Serializable;

import jakarta.persistence.Embeddable;

@Embeddable
public record WishlistItemId(Long userId, Long bookId) implements Serializable {
}
