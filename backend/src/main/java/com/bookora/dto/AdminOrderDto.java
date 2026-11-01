package com.bookora.dto;

public record AdminOrderDto(OrderDto order, String customerName, String customerEmail) {
}
