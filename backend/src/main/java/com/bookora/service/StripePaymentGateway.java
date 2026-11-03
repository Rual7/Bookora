package com.bookora.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.bookora.exception.BadRequestException;
import com.bookora.exception.ServiceUnavailableException;
import com.bookora.model.CustomerOrder;
import com.bookora.model.OrderItem;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class StripePaymentGateway implements PaymentGateway {

	// Stripe rejects events whose timestamp is older than this, which stops a captured request from being replayed.
	private static final long SIGNATURE_TOLERANCE_SECONDS = 300;

	private static final ObjectMapper JSON = new ObjectMapper();

	private final String secretKey;
	private final String webhookSecret;
	private final String publicBaseUrl;

	public StripePaymentGateway(@Value("${bookora.stripe.secret-key}") String secretKey,
			@Value("${bookora.stripe.webhook-secret}") String webhookSecret,
			@Value("${bookora.public-base-url}") String publicBaseUrl) {
		this.secretKey = secretKey;
		this.webhookSecret = webhookSecret;
		this.publicBaseUrl = publicBaseUrl;
	}

	@Override
	public boolean isConfigured() {
		return !secretKey.isBlank();
	}

	@Override
	public PaymentGateway.Session createSession(CustomerOrder order, Instant expiresAt) {
		String orderUrl = publicBaseUrl + "/orders/" + order.getOrderNumber();
		SessionCreateParams.Builder params = SessionCreateParams.builder()
				.setMode(SessionCreateParams.Mode.PAYMENT)
				.setClientReferenceId(order.getOrderNumber())
				.setCustomerEmail(order.getUser().getEmail())
				.setSuccessUrl(orderUrl + "?payment=success")
				.setCancelUrl(orderUrl + "?payment=cancelled")
				.setExpiresAt(expiresAt.getEpochSecond())
				.putMetadata("orderNumber", order.getOrderNumber());

		for (OrderItem item : order.getItems()) {
			String name = item.getProductTitle() + " (" + item.getProductFormat().name().toLowerCase(Locale.ROOT) + ")";
			params.addLineItem(lineItem(name, item.getUnitPrice(), item.getQuantity(), order.getCurrency()));
		}
		if (order.getShippingCost().signum() > 0) {
			params.addLineItem(lineItem("Delivery", order.getShippingCost(), 1, order.getCurrency()));
		}

		try {
			com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.create(params.build(), options());
			return new PaymentGateway.Session(session.getId(), session.getUrl());
		} catch (StripeException _) {
			throw new ServiceUnavailableException("The payment service is not available right now. Please try again.");
		}
	}

	@Override
	public Optional<String> findOpenSessionUrl(String sessionId) {
		try {
			com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.retrieve(sessionId, options());
			return "open".equals(session.getStatus()) ? Optional.ofNullable(session.getUrl()) : Optional.empty();
		} catch (StripeException _) {
			return Optional.empty();
		}
	}

	@Override
	public Event readEvent(String payload, String signatureHeader) {
		if (webhookSecret.isBlank() || signatureHeader == null) {
			throw new BadRequestException("Invalid signature");
		}
		try {
			Webhook.Signature.verifyHeader(payload, signatureHeader, webhookSecret, SIGNATURE_TOLERANCE_SECONDS);
		} catch (SignatureVerificationException | RuntimeException _) {
			throw new BadRequestException("Invalid signature");
		}

		// Only a few fields are needed, so the JSON is read directly instead of through the SDK's versioned classes.
		JsonNode event = JSON.readTree(payload);
		JsonNode object = event.path("data").path("object");
		return new Event(text(event, "id"), text(event, "type"), text(object, "id"), text(object, "payment_intent"),
				"paid".equals(text(object, "payment_status")));
	}

	private SessionCreateParams.LineItem lineItem(String name, BigDecimal unitPrice, int quantity, String currency) {
		return SessionCreateParams.LineItem.builder()
				.setQuantity((long) quantity)
				.setPriceData(SessionCreateParams.LineItem.PriceData.builder()
						.setCurrency(currency.toLowerCase(Locale.ROOT))
						.setUnitAmount(unitPrice.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact())
						.setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder().setName(name).build())
						.build())
				.build();
	}

	private com.stripe.net.RequestOptions options() {
		if (!isConfigured()) {
			throw new ServiceUnavailableException("Online payment is not set up yet.");
		}
		return com.stripe.net.RequestOptions.builder().setApiKey(secretKey).build();
	}

	private static String text(JsonNode json, String field) {
		return json.path(field).textValue();
	}

}
