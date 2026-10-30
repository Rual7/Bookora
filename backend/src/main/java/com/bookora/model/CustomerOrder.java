package com.bookora.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_order")
public class CustomerOrder extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Column(name = "order_number", nullable = false, unique = true)
	private String orderNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status = OrderStatus.PENDING_PAYMENT;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "shipping_address_id")
	private OrderAddress shippingAddress;

	@Column(name = "shipping_cost", nullable = false, precision = 10, scale = 2)
	private BigDecimal shippingCost = BigDecimal.ZERO;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "paid_at")
	private Instant paidAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	private List<OrderItem> items = new ArrayList<>();

	protected CustomerOrder() {
	}

	public CustomerOrder(AppUser user, String orderNumber, String currency, OrderAddress shippingAddress) {
		this.user = user;
		this.orderNumber = orderNumber;
		this.currency = currency;
		this.shippingAddress = shippingAddress;
	}

	public void addItem(Product product, int quantity) {
		items.add(new OrderItem(this, product, quantity));
	}

	public BigDecimal getSubtotal() {
		return items.stream().map(OrderItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public void setShippingCost(BigDecimal shippingCost) {
		this.shippingCost = shippingCost;
	}

	public BigDecimal getTotal() {
		return getSubtotal().add(shippingCost);
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public OrderAddress getShippingAddress() {
		return shippingAddress;
	}

	public BigDecimal getShippingCost() {
		return shippingCost;
	}

	public String getCurrency() {
		return currency;
	}

	public Instant getPaidAt() {
		return paidAt;
	}

	public List<OrderItem> getItems() {
		return items;
	}

}
