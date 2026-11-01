package com.bookora.service;

import java.time.Instant;
import java.util.Optional;

import com.bookora.model.CustomerOrder;

/** The payment provider (Stripe). Kept behind an interface so the payment rules can be tested without the network. */
public interface PaymentGateway {

	/** What we need to remember about a payment session that was just opened. */
	record Session(String id, String url) {
	}

	/** A verified webhook event. `paid` is only meaningful for a completed checkout session. */
	record Event(String id, String type, String sessionId, String paymentId, boolean paid) {
	}

	boolean isConfigured();

	Session createSession(CustomerOrder order, Instant expiresAt);

	/** The payment page of a session that is still open, or empty if it was completed or expired. */
	Optional<String> findOpenSessionUrl(String sessionId);

	/** Checks the signature and reads the event. Throws BadRequestException when the signature is wrong. */
	Event readEvent(String payload, String signatureHeader);

}
