package com.bookora.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.dto.PaymentLinkDto;
import com.bookora.service.PaymentService;

@RestController
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping("/api/orders/{orderNumber}/payment")
	public PaymentLinkDto pay(Authentication auth, @PathVariable String orderNumber) {
		return paymentService.startPayment(auth.getName(), orderNumber);
	}

	/** Called by Stripe, not by the browser: no session and no CSRF token, so the signature is what proves who sent it. */
	@PostMapping("/api/webhooks/stripe")
	public void stripeWebhook(@RequestBody String payload,
			@RequestHeader(value = "Stripe-Signature", required = false) String signature) {
		paymentService.handleWebhook(payload, signature);
	}

}
