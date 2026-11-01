package com.bookora.service;

import com.bookora.dto.PaymentLinkDto;

public interface PaymentService {

	/** Opens (or reuses) the payment page for one of the customer's unpaid orders. */
	PaymentLinkDto startPayment(String email, String orderNumber);

	/** Handles a webhook from the payment provider. Unknown or repeated events change nothing. */
	void handleWebhook(String payload, String signatureHeader);

	/** Cancels unpaid orders nobody is paying for and releases their stock. Returns how many were cancelled. */
	int cancelStaleOrders();

}
