package com.bookora.model;

public enum OrderStatus {
	PENDING_PAYMENT, PAID, SHIPPED, DELIVERED, CANCELLED;

	/** The money arrived: the order is paid, shipped or delivered. */
	public boolean isPaid() {
		return this == PAID || this == SHIPPED || this == DELIVERED;
	}
}
