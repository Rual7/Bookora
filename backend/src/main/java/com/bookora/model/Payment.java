package com.bookora.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One Stripe Checkout session opened for an order. An order can have several if the first one expires. */
@Entity
@Table(name = "payment")
public class Payment extends BaseEntity {

	private static final String PROVIDER = "STRIPE";

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private CustomerOrder order;

	@Column(nullable = false)
	private String provider = PROVIDER;

	@Column(name = "provider_session_id", nullable = false, unique = true)
	private String providerSessionId;

	@Column(name = "provider_payment_id")
	private String providerPaymentId;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(nullable = false, length = 3)
	private String currency;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status = PaymentStatus.PENDING;

	@Column(name = "expires_at")
	private Instant expiresAt;

	@Column(name = "paid_at")
	private Instant paidAt;

	protected Payment() {
	}

	public Payment(CustomerOrder order, String providerSessionId, Instant expiresAt) {
		this.order = order;
		this.providerSessionId = providerSessionId;
		this.amount = order.getTotal();
		this.currency = order.getCurrency();
		this.expiresAt = expiresAt;
	}

	public void markSucceeded(String providerPaymentId) {
		this.status = PaymentStatus.SUCCEEDED;
		this.providerPaymentId = providerPaymentId;
		this.paidAt = Instant.now();
	}

	public void markExpired() {
		this.status = PaymentStatus.EXPIRED;
	}

	public CustomerOrder getOrder() {
		return order;
	}

	public String getProviderSessionId() {
		return providerSessionId;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

}
