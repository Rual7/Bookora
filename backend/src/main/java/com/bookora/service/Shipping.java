package com.bookora.service;

import java.math.BigDecimal;

/** Delivery pricing, shared by the cart and by checkout so both always show the same number. */
final class Shipping {

	static final BigDecimal STANDARD_COST = new BigDecimal("2.50");
	static final BigDecimal FREE_ABOVE = new BigDecimal("40.00");

	private Shipping() {
	}

	/** E-books are not delivered; printed books cost the standard fee unless the order is above the free limit. */
	static BigDecimal cost(BigDecimal subtotal, boolean hasPrintedItems) {
		if (!hasPrintedItems || subtotal.compareTo(FREE_ABOVE) > 0) {
			return BigDecimal.ZERO;
		}
		return STANDARD_COST;
	}

}
