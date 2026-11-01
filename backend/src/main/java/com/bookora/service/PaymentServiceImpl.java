package com.bookora.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.dto.PaymentLinkDto;
import com.bookora.exception.ConflictException;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.exception.ServiceUnavailableException;
import com.bookora.model.AppUser;
import com.bookora.model.CustomerOrder;
import com.bookora.model.OrderStatus;
import com.bookora.model.Payment;
import com.bookora.model.PaymentStatus;
import com.bookora.model.ProcessedWebhookEvent;
import com.bookora.repository.OrderRepository;
import com.bookora.repository.PaymentRepository;
import com.bookora.repository.ProcessedWebhookEventRepository;
import com.bookora.repository.UserRepository;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

	private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

	private static final String PROVIDER = "STRIPE";
	// Stripe needs a session to last at least 30 minutes.
	static final Duration SESSION_LIFETIME = Duration.ofMinutes(30);
	// An order nobody pays for within this time is cancelled, so the books are not held back forever.
	static final Duration UNPAID_ORDER_LIFETIME = Duration.ofHours(1);

	private final PaymentGateway gateway;
	private final OrderRepository orders;
	private final PaymentRepository payments;
	private final ProcessedWebhookEventRepository processedEvents;
	private final UserRepository users;
	private final OrderCancellation cancellation;

	public PaymentServiceImpl(PaymentGateway gateway, OrderRepository orders, PaymentRepository payments,
			ProcessedWebhookEventRepository processedEvents, UserRepository users, OrderCancellation cancellation) {
		this.gateway = gateway;
		this.orders = orders;
		this.payments = payments;
		this.processedEvents = processedEvents;
		this.users = users;
		this.cancellation = cancellation;
	}

	@Override
	public PaymentLinkDto startPayment(String email, String orderNumber) {
		if (!gateway.isConfigured()) {
			throw new ServiceUnavailableException("Online payment is not set up yet.");
		}
		AppUser user = users.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		CustomerOrder order = orders.findByOrderNumberAndUserId(orderNumber, user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));
		if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
			throw new ConflictException("This order does not need a payment");
		}

		Optional<String> openUrl = payments.findFirstByOrderIdAndStatusOrderByIdDesc(order.getId(), PaymentStatus.PENDING)
				.filter(payment -> payment.getExpiresAt().isAfter(Instant.now()))
				.flatMap(payment -> gateway.findOpenSessionUrl(payment.getProviderSessionId()));
		if (openUrl.isPresent()) {
			return new PaymentLinkDto(openUrl.get());
		}

		Instant expiresAt = Instant.now().plus(SESSION_LIFETIME);
		PaymentGateway.Session session = gateway.createSession(order, expiresAt);
		payments.save(new Payment(order, session.id(), expiresAt));
		log.info("Payment started: order={}, session={}", order.getOrderNumber(), session.id());
		return new PaymentLinkDto(session.url());
	}

	@Override
	public void handleWebhook(String payload, String signatureHeader) {
		PaymentGateway.Event event = gateway.readEvent(payload, signatureHeader);

		// Stripe sends an event again when it did not get an answer in time; the second one must change nothing.
		if (processedEvents.existsByProviderAndProviderEventId(PROVIDER, event.id())) {
			log.info("Webhook event ignored, already processed: id={}", event.id());
			return;
		}
		processedEvents.save(new ProcessedWebhookEvent(PROVIDER, event.id(), event.type()));

		switch (event.type()) {
			case "checkout.session.completed" -> onCompleted(event);
			case "checkout.session.expired" -> onExpired(event);
			default -> log.info("Webhook event of no interest: type={}", event.type());
		}
	}

	@Override
	public int cancelStaleOrders() {
		Instant now = Instant.now();
		var stale = orders.findStaleUnpaid(now.minus(UNPAID_ORDER_LIFETIME), now);
		stale.forEach(cancellation::cancel);
		return stale.size();
	}

	private void onCompleted(PaymentGateway.Event event) {
		if (!event.paid()) {
			return;
		}
		Optional<Payment> found = payments.findByProviderSessionId(event.sessionId());
		if (found.isEmpty()) {
			log.warn("Payment for an unknown session: session={}", event.sessionId());
			return;
		}
		Payment payment = found.get();
		CustomerOrder order = payment.getOrder();
		payment.markSucceeded(event.paymentId());

		if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
			order.markPaid();
			log.info("Order paid: number={}", order.getOrderNumber());
		} else {
			// The money arrived after the order was cancelled; this needs a refund by hand.
			log.error("Payment received for an order that is {}: number={}, session={}", order.getStatus(),
					order.getOrderNumber(), event.sessionId());
		}
	}

	private void onExpired(PaymentGateway.Event event) {
		Optional<Payment> found = payments.findByProviderSessionId(event.sessionId());
		if (found.isEmpty()) {
			return;
		}
		Payment payment = found.get();
		payment.markExpired();

		CustomerOrder order = payment.getOrder();
		boolean anotherSessionOpen = payments.existsByOrderIdAndStatusAndExpiresAtAfter(order.getId(),
				PaymentStatus.PENDING, Instant.now());
		if (order.getStatus() == OrderStatus.PENDING_PAYMENT && !anotherSessionOpen) {
			cancellation.cancel(order);
		}
	}

}
