package com.bookora.repository;

/** How many orders one customer placed. */
public record OrderCount(Long userId, long count) {
}
